package com.bandlab.metro.extensions.component

import com.bandlab.metro.extensions.utils.*
import com.fueledbycaffeine.autoservice.AutoService
import dev.zacsweers.metro.compiler.MetroOptions
import dev.zacsweers.metro.compiler.api.fir.MetroFirDeclarationGenerationExtension
import dev.zacsweers.metro.compiler.compat.CompatContext
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildCollectionLiteral
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import com.bandlab.metro.extensions.component.ContributesComponentIds as Ids

/**
 * This FIR declaration generator generates a dependency graph for the feature that is annotated with
 * [Ids.contributesComponent].
 *
 * ```kotlin
 * @ContributesComponent(appDependencies = [MyActivity.ServiceProvider::class])
 * class MyActivity : CommonActivity<Param>() {
 *
 *   data class Param(val int: Int)
 *
 *   interface ServiceProvider {
 *     val myDependency: MyDependency
 *   }
 *
 *   // This extension generates:
 *   @IROnlyFactories
 *   @ActivityScope
 *   @DependencyGraph(
 *     scope = MyActivity::class,
 *     bindingContainers = [DefaultActivityDependencies::class]
 *   )
 *   interface FeatureGraph : MembersInjectorProvider<MyActivity> {
 *
 *     @Provides
 *     fun provideBaseType(feature: MyActivity): CommonActivity<*> = feature
 *
 *     // If the feature has a param
 *     @Provides
 *     fun provideParam(feature: MyActivity): MyActivity.Param = feature.param
 *
 *     interface Factory : GraphFactory<MyActivity, FeatureServiceProvider, EmptyExtraDependencies, FeatureGraph>
 *   }
 *
 *   @ContributesTo(AppScope::class)
 *   interface FeatureServiceProvider :
 *     ServiceProvider,
 *     CommonActivity.ServiceProvider,
 *     DefaultScreenServiceProvider
 * }
 * ```
 */
public class ContributesComponentFir(session: FirSession, compatContext: CompatContext) :
    MetroFirDeclarationGenerationExtension(session), CompatContext by compatContext {

    /**
     * Represents the resolved component type of an annotated class, determined by its super type.
     */
    private sealed interface ComponentType {
        data class Activity(val superTypeRef: FirTypeRef) : ComponentType
        data class Page(
            val superTypeRef: FirTypeRef,
            val hasParam: Boolean,
        ) : ComponentType

        data object Fragment : ComponentType
        data object Service : ComponentType
        data object Worker : ComponentType
        data object BroadcastReceiver : ComponentType
    }

    /**
     * Cache of symbol to its resolved [ComponentType]. Populated in [generateFeatureGraph] (the
     * earliest call site) and reused in [generateFactory] and [generateFeatureServiceProvider].
     */
    private val componentTypeCache = mutableMapOf<ClassId, ComponentType>()

    override val enableFirInIde: Boolean
        get() = true

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Ids.componentPredicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        return when {
            session.predicateBasedProvider.matches(Ids.componentPredicate, classSymbol) ->
                setOf(Ids.graphName, Ids.featureServiceProviderName)

            classSymbol.origin == Key.origin && classSymbol.classId.shortClassName == Ids.graphName ->
                setOf(Ids.nestedFactoryName)

            else -> emptySet()
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        // Generate FeatureGraph inside the annotated class
        if (name == Ids.graphName &&
            session.predicateBasedProvider.matches(Ids.componentPredicate, owner)
        ) {
            return generateFeatureGraph(owner)
        }
        // Generate Factory inside FeatureGraph
        if (name == Ids.nestedFactoryName &&
            owner.origin == Key.origin &&
            owner.classId.shortClassName == Ids.graphName
        ) {
            return generateFactory(owner)
        }
        // Generate FeatureServiceProvider inside the annotated class
        if (name == Ids.featureServiceProviderName &&
            session.predicateBasedProvider.matches(Ids.componentPredicate, owner)
        ) {
            return generateFeatureServiceProvider(owner)
        }
        return null
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        if (classSymbol.origin != Key.origin) return emptySet()
        if (classSymbol.classId.shortClassName == Ids.graphName) {
            return setOf(Ids.provideBaseTypeName, Ids.provideParamName, Ids.provideParamFlowName)
        }
        return emptySet()
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        val ownerClassId = callableId.classId ?: return emptyList()
        val owner = context?.owner ?: return emptyList()
        if (owner.origin != Key.origin) return emptyList()

        // Generate "provideBaseType" function for FeatureGraph
        if (callableId.callableName == Ids.provideBaseTypeName &&
            ownerClassId.shortClassName == Ids.graphName
        ) {
            val provideBaseTypeFunction = generateProvideBaseTypeFunction(owner) ?: return emptyList()
            return listOf(provideBaseTypeFunction)
        }

        // Generate "provideParam" function for FeatureGraph
        if (callableId.callableName == Ids.provideParamName &&
            ownerClassId.shortClassName == Ids.graphName
        ) {
            val provideParamFunction = generateProvideParamFunction(owner) ?: return emptyList()
            return listOf(provideParamFunction)
        }

        // Generate "provideParamFlow" function for FeatureGraph (ParamPage only)
        if (callableId.callableName == Ids.provideParamFlowName &&
            ownerClassId.shortClassName == Ids.graphName
        ) {
            val provideParamFlowFunction = generateProvideParamFlowFunction(owner) ?: return emptyList()
            return listOf(provideParamFlowFunction)
        }

        return emptyList()
    }

    private fun generateFeatureGraph(owner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val nestedClassId = owner.classId.createNestedClassId(Ids.graphName)
        val classSymbol = FirRegularClassSymbol(nestedClassId)
        val annotation = owner.getAnnotationByClassId(Ids.contributesComponent, session) as? FirAnnotationCall

        val componentType = resolveComponentType(owner)
        val componentScope = when (componentType) {
            is ComponentType.Activity -> Ids.activityScope
            is ComponentType.Page -> Ids.pageScope
            ComponentType.Fragment -> Ids.fragmentScope
            ComponentType.Service -> Ids.serviceScope
            ComponentType.Worker -> Ids.workerScope
            ComponentType.BroadcastReceiver -> Ids.broadcastReceiverScope
        }

        val featureGraph = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = owner.source
            classKind = ClassKind.INTERFACE
            scopeProvider = session.kotlinScopeProvider
            this.name = nestedClassId.shortClassName
            symbol = classSymbol
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(owner, forClass = true),
            )

            annotations += buildSimpleAnnotation(componentScope)

            if (componentType is ComponentType.Page) {
                superTypeRefs += buildResolvedTypeRef {
                    val viewModelType = componentType.superTypeRef.unwrapType()!!
                    coneType = Ids.pageInjector.constructClassLikeType(arrayOf(viewModelType))
                }
            } else {
                superTypeRefs += buildResolvedTypeRef {
                    coneType = Ids.membersInjectorProvider.constructClassLikeType(
                        arrayOf(owner.defaultType())
                    )
                }
            }

            // Metro's FIR cannot see generated providers inside the graph, so we'll need to annotate the graph
            // with @IROnlyFactoties to generate factories in IR.
            annotations += buildSimpleAnnotation(ClassIds.irOnlyFactories)

            // @DependencyGraph annotation
            annotations += buildSimpleAnnotation(
                classId = ClassIds.dependencyGraph,
                argumentMapping = buildAnnotationArgumentMapping {
                    val graphMarkerExpr = annotation?.argumentList?.arguments
                        ?.filterIsInstance<FirNamedArgumentExpression>()
                        ?.find { it.name == Ids.graphMarkerName }
                        ?.expression

                    mapping[ClassIds.scopeName] = graphMarkerExpr ?: owner.getClassCall()
                    mapping[ClassIds.bindingContainersName] = buildCollectionLiteral {
                        val elementType = StandardClassIds.KClass.constructClassLikeType(arrayOf(ConeStarProjection))
                        coneTypeOrNull = StandardClassIds.Array.constructClassLikeType(arrayOf(elementType))
                        argumentList = buildArgumentList {
                            val defaultDependenciesIds = when (componentType) {
                                is ComponentType.Activity -> setOf(Ids.defaultActivityDeps)
                                ComponentType.Fragment -> setOf(Ids.defaultFragmentDeps)
                                is ComponentType.Page -> setOf(
                                    Ids.defaultPageDependencies,
                                    Ids.pageGraphDependenciesModule,
                                )

                                ComponentType.BroadcastReceiver, ComponentType.Service, ComponentType.Worker -> emptySet()
                            }

                            defaultDependenciesIds.forEach { dependenciesId ->
                                session.symbolProvider.getClassLikeSymbolByClassId(dependenciesId)?.let {
                                    arguments += it.getClassCall()
                                }
                            }
                        }
                    }
                }
            )
        }
        return featureGraph.symbol
    }

    private fun generateProvideBaseTypeFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val annotatedClassId = owner.classId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        val commonActivitySuperType = annotatedSymbol.resolvedSuperTypes.find { it.classId == Ids.commonActivity }
        val pageSuperType = annotatedSymbol.resolvedSuperTypes.find { it.classId == Ids.paramPage }
            ?: annotatedSymbol.resolvedSuperTypes.find { it.classId == Ids.page }

        val baseType = when {
            commonActivitySuperType != null -> Ids.commonActivity.constructClassLikeType(arrayOf(ConeStarProjection))
            pageSuperType != null -> Ids.page.constructClassLikeType(arrayOf(ConeStarProjection))
            else -> return null
        }

        val provideBaseTypeFunction = createMemberFunction(
            owner,
            Key,
            Ids.provideBaseTypeName,
            baseType
        ) {
            valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)
        }
        provideBaseTypeFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
        return provideBaseTypeFunction.symbol as FirNamedFunctionSymbol
    }

    private fun generateProvideParamFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val annotatedClassId = owner.classId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        return when (val componentType = resolveComponentType(annotatedSymbol)) {
            is ComponentType.Activity -> {
                val typeArg = componentType.superTypeRef.unwrapType(0) ?: return null
                val paramType = typeArg as? ConeKotlinType ?: return null
                if (paramType.classId == StandardClassIds.Unit) return null
                validateParamType(paramType)

                val provideParamFunction =
                    createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                        valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)
                    }
                provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
                provideParamFunction.symbol as FirNamedFunctionSymbol
            }

            is ComponentType.Page if componentType.hasParam -> {
                // ParamPage<ViewModel, Param> - extract the 2nd type arg (Param)
                val paramTypeArg = componentType.superTypeRef.unwrapType(1) ?: return null
                val paramType = paramTypeArg as? ConeKotlinType ?: return null
                validateParamType(paramType)

                val pageGraphDepsType = Ids.pageGraphDependencies.constructClassLikeType()
                val provideParamFunction =
                    createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                        valueParameter("pageGraphDependencies".asName(), pageGraphDepsType, key = Key)
                    }
                provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
                provideParamFunction.symbol as FirNamedFunctionSymbol
            }

            ComponentType.BroadcastReceiver, ComponentType.Fragment,
            is ComponentType.Page, ComponentType.Service, ComponentType.Worker -> null
        }
    }

    private fun generateProvideParamFlowFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val annotatedClassId = owner.classId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        val paramPageSuperType = annotatedSymbol.resolvedSuperTypes.find { it.classId == Ids.paramPage }
            ?: return null

        val paramTypeArg = paramPageSuperType.typeArguments.getOrNull(1) ?: return null
        val paramType = paramTypeArg as? ConeKotlinType ?: return null

        val flowOfParamType = Ids.coroutinesFlow.constructClassLikeType(arrayOf(paramType))
        val pageParamFlowProviderType = Ids.pageParamFlowProvider.constructClassLikeType()

        val provideParamFlowFunction =
            createMemberFunction(owner, Key, Ids.provideParamFlowName, flowOfParamType) {
                valueParameter("provider".asName(), pageParamFlowProviderType, key = Key)
                valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)
                valueParameter(Ids.initialParamName, paramType, key = Key)
            }
        provideParamFlowFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
        return provideParamFlowFunction.symbol as FirNamedFunctionSymbol
    }

    private fun generateFactory(featureGraphOwner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val factoryClassId = featureGraphOwner.classId.createNestedClassId(Ids.nestedFactoryName)
        val factorySymbol = FirRegularClassSymbol(factoryClassId)
        val ownerClassId = featureGraphOwner.classId.parentClassId!!
        val owner = session.symbolProvider.getClassLikeSymbolByClassId(ownerClassId) as FirRegularClassSymbol

        val factory = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = featureGraphOwner.source
            classKind = ClassKind.INTERFACE
            scopeProvider = session.kotlinScopeProvider
            this.name = factoryClassId.shortClassName
            symbol = factorySymbol
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(featureGraphOwner, forClass = true),
            )
            annotations += buildSimpleAnnotation(ClassIds.dependencyGraphFactory)

            val baseFactoryType = when (resolveComponentType(owner)) {
                is ComponentType.Activity -> Ids.graphFactory
                is ComponentType.Page -> Ids.pageGraphFactory
                ComponentType.BroadcastReceiver,
                ComponentType.Fragment,
                ComponentType.Service,
                ComponentType.Worker,
                    -> null
            }
            if (baseFactoryType != null) {
                superTypeRefs += buildResolvedTypeRef {
                    val rootType = ownerClassId.constructClassLikeType()
                    val serviceProviderType =
                        ownerClassId.createNestedClassId(Ids.featureServiceProviderName).constructClassLikeType()
                    val graphType = featureGraphOwner.classId.constructClassLikeType()

                    val extraDependenciesType = owner.resolveExtraDependencies()

                    coneType = baseFactoryType.constructClassLikeType(
                        arrayOf(
                            rootType,
                            serviceProviderType,
                            extraDependenciesType,
                            graphType
                        )
                    )
                }
            }
        }
        return factory.symbol
    }

    /**
     * Resolves the extraDependencies type from the @ContributesComponent annotation.
     * Returns EmptyExtraDependencies if extraDependencies is Nothing::class.
     */
    private fun FirClassSymbol<*>.resolveExtraDependencies(): ConeKotlinType {
        val annotation = getAnnotationByClassId(Ids.contributesComponent, session)
            ?: return Ids.emptyExtraDependencies.constructClassLikeType()

        val rawExpr = annotation.argumentMapping.mapping[Ids.extraDependenciesName]
            ?: (annotation as? FirAnnotationCall)?.argumentList?.arguments
                ?.filterIsInstance<FirNamedArgumentExpression>()
                ?.find { it.name == Ids.extraDependenciesName }
            ?: return Ids.emptyExtraDependencies.constructClassLikeType()

        val expr = if (rawExpr is FirNamedArgumentExpression) rawExpr.expression else rawExpr

        val getClassCall = expr as? FirGetClassCall
            ?: return Ids.emptyExtraDependencies.constructClassLikeType()

        val classId = when (val argument = getClassCall.argument) {
            is FirResolvedQualifier -> argument.classId
            else -> argument.extractClassId(this.classId, session)
        } ?: return Ids.emptyExtraDependencies.constructClassLikeType()

        return if (classId == StandardClassIds.Nothing) {
            Ids.emptyExtraDependencies.constructClassLikeType()
        } else {
            classId.constructClassLikeType()
        }
    }

    private fun generateFeatureServiceProvider(owner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val nestedClassId = owner.classId.createNestedClassId(Ids.featureServiceProviderName)
        val classSymbol = FirRegularClassSymbol(nestedClassId)

        val componentType = resolveComponentType(owner)
        val appScopeSymbol = session.symbolProvider.getClassLikeSymbolByClassId(ClassIds.appScope)!!

        val featureServiceProvider = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = owner.source
            classKind = ClassKind.INTERFACE
            scopeProvider = session.kotlinScopeProvider
            this.name = nestedClassId.shortClassName
            symbol = classSymbol
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(owner, forClass = true),
            )

            superTypeRefs += buildResolvedTypeRef { coneType = owner.requireAppDependencies() }

            if (componentType is ComponentType.Activity) {
                superTypeRefs += buildResolvedTypeRef {
                    coneType = Ids.commonActivityServiceProvider.constructClassLikeType()
                }
            }

            // Add DefaultScreenServiceProvider for screens
            if (
                componentType is ComponentType.Activity ||
                componentType is ComponentType.Fragment ||
                componentType is ComponentType.Page
            ) {
                superTypeRefs += buildResolvedTypeRef {
                    coneType = Ids.defaultScreenServiceProvider.constructClassLikeType()
                }
            }

            // @ContributesTo(AppScope::class)
            annotations += buildSimpleAnnotation(
                classId = ClassIds.contributesTo,
                argumentMapping = buildAnnotationArgumentMapping {
                    mapping[ClassIds.scopeName] = appScopeSymbol.getClassCall()
                }
            )
        }
        return featureServiceProvider.symbol
    }

    private fun resolveComponentType(owner: FirClassSymbol<*>): ComponentType {
        return componentTypeCache.getOrPut(owner.classId) {
            for (classId in Ids.activityTypes) {
                val ref = owner.findSuperTypeRef(classId)
                if (ref != null) return@getOrPut ComponentType.Activity(ref)
            }
            for (classId in Ids.pageTypes) {
                val ref = owner.findSuperTypeRef(classId)
                if (ref != null) return@getOrPut ComponentType.Page(ref, hasParam = classId == Ids.paramPage)
            }
            for (classId in Ids.fragmentTypes) {
                if (owner.findSuperTypeRef(classId) != null) return@getOrPut ComponentType.Fragment
            }
            for (classId in Ids.serviceTypes) {
                if (owner.findSuperTypeRef(classId) != null) return@getOrPut ComponentType.Service
            }
            for (classId in Ids.workerTypes) {
                if (owner.findSuperTypeRef(classId) != null) return@getOrPut ComponentType.Worker
            }
            for (classId in Ids.broadcastReceiverTypes) {
                if (owner.findSuperTypeRef(classId) != null) return@getOrPut ComponentType.BroadcastReceiver
            }
            error(
                "Cannot resolve component type for ${owner.classId}. " +
                    "Class must extend one of: CommonActivity, CommonFragment, Page, ParamPage, Service, CoroutineWorker, or BroadcastReceiver"
            )
        }
    }

    private val restrictedParamsType = listOf(
        StandardClassIds.String,
        StandardClassIds.Int,
        StandardClassIds.Long,
        StandardClassIds.Boolean,
        StandardClassIds.Float,
        StandardClassIds.Double,
        StandardClassIds.Char,
        StandardClassIds.Byte,
        StandardClassIds.Short,
    )

    //TODO: This can be moved to a FIR checker if we want
    private fun validateParamType(paramType: ConeKotlinType) {
        require(paramType.classId !in restrictedParamsType) {
            "Parameter type ${paramType.renderReadable()} is a restricted primitive type. Use a wrapper class instead."
        }
    }

    /**
     * Validates that the annotated class has the required @ContributesComponent annotation and
     * retrieves the appDependencies type from it.
     */
    private fun FirClassSymbol<*>.requireAppDependencies(): ConeKotlinType {
        val annotation = getAnnotationByClassId(Ids.contributesComponent, session)
            ?: error("Cannot find @ContributesComponent annotation")

        // Try resolved argument mapping first, then fall back to argument list
        val rawExpr = annotation.argumentMapping.mapping[Ids.appDependenciesName]
            ?: (annotation as? FirAnnotationCall)?.argumentList?.arguments?.firstOrNull()
            ?: error("Cannot find @ContributesComponent.appDependencies argument")

        // Unwrap FirNamedArgumentExpression if present
        val expr = if (rawExpr is FirNamedArgumentExpression) rawExpr.expression else rawExpr

        // Extract the type from the GetClassCall
        val getClassCall = expr as? FirGetClassCall
            ?: error("Expected a GetClassCall for appDependencies argument, got: ${expr::class.simpleName}")

        return when (val argument = getClassCall.argument) {
            is FirResolvedQualifier -> {
                val classId = argument.classId
                    ?: error("FirResolvedQualifier has no classId")
                classId.constructClassLikeType()
            }

            else -> {
                // At early FIR stages, the argument may not be fully resolved.
                // Extract the ClassId by collecting name parts from the property access chain.
                val classId = argument.extractClassId(this.classId, session)
                    ?: error("Cannot extract ClassId from GetClassCall argument: ${argument::class.simpleName}")
                classId.constructClassLikeType()
            }
        }
    }

    override fun getContributionHints(): List<ContributionHint> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(Ids.componentPredicate)
            .filterIsInstance<FirRegularClassSymbol>()
            .map { classSymbol ->
                val serviceProvider = classSymbol.classId.createNestedClassId(Ids.featureServiceProviderName)
                ContributionHint(contributingClassId = serviceProvider, scope = ClassIds.appScope)
            }
    }

    internal object Key : GeneratedDeclarationKey()

    @AutoService
    public class Factory : MetroFirDeclarationGenerationExtension.Factory {
        override fun create(
            session: FirSession,
            options: MetroOptions,
            compatContext: CompatContext,
        ): MetroFirDeclarationGenerationExtension = ContributesComponentFir(session, compatContext)
    }
}
