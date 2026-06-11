package com.bandlab.metro.station.graph

import com.bandlab.metro.station.utils.*
import dev.zacsweers.metro.compiler.MetroOptions
import dev.zacsweers.metro.compiler.api.fir.MetroFirDeclarationGenerationExtension
import dev.zacsweers.metro.compiler.compat.CompatContext
import dev.zacsweers.metro.compiler.fir.MetroFirTypeResolver
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
import com.bandlab.metro.station.graph.MetroStationIds as Ids

/**
 * This FIR declaration generator generates a dependency graph for the feature that is annotated with [Ids.metroStation].
 */
public class MetroStationFir(session: FirSession, compatContext: CompatContext) :
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
        data object Others : ComponentType
    }

    /**
     * Cache of symbol to its resolved [ComponentType]. Populated in [generateFeatureGraph] (the
     * earliest call site) and reused in [generateFactory] and [generateFeatureServiceProvider].
     */
    private val componentTypeCache = mutableMapOf<ClassId, ComponentType>()

    private val typeResolverFactory by lazy { MetroFirTypeResolver.Factory(session) }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Ids.metroStationPredicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        return when {
            session.predicateBasedProvider.matches(Ids.metroStationPredicate, classSymbol) ->
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
            session.predicateBasedProvider.matches(Ids.metroStationPredicate, owner)
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
            session.predicateBasedProvider.matches(Ids.metroStationPredicate, owner)
        ) {
            return generateFeatureServiceProvider(owner)
        }
        return null
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        // Generate inject()/injectViewModel() overrides on the annotated class itself
        if (session.predicateBasedProvider.matches(Ids.metroStationPredicate, classSymbol)) {
            return when (resolveComponentType(classSymbol)) {
                is ComponentType.Activity -> setOf(Ids.injectName)
                is ComponentType.Page -> setOf(Ids.injectViewModelName)
                ComponentType.Fragment, ComponentType.Others -> emptySet()
            }
        }
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

        // Generate inject()/injectViewModel() overrides on the annotated class
        if (session.predicateBasedProvider.matches(Ids.metroStationPredicate, owner)) {
            return when (callableId.callableName) {
                Ids.injectName -> listOfNotNull(generateInjectFunction(owner))
                Ids.injectViewModelName -> listOfNotNull(generateInjectViewModelFunction(owner))
                else -> emptyList()
            }
        }

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
        val annotation = owner.getAnnotationByClassId(Ids.metroStation, session) as? FirAnnotationCall

        val componentType = resolveComponentType(owner)
        val componentScope = when (componentType) {
            is ComponentType.Activity -> Ids.activityScope
            is ComponentType.Page -> Ids.pageScope
            ComponentType.Fragment -> Ids.fragmentScope
            ComponentType.Others -> null
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

            if (componentScope != null) {
                annotations += buildSimpleAnnotation(componentScope)
            }

            if (componentType is ComponentType.Page) {
                superTypeRefs += buildResolvedTypeRef {
                    val viewModelType = resolvePageViewModelType(componentType.superTypeRef, owner, session)
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
                                is ComponentType.Page -> setOf(Ids.defaultPageDependencies)

                                ComponentType.Others -> emptySet()
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
        val annotatedClassId = owner.classId.outerClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        val componentType = resolveComponentType(annotatedSymbol)
        val baseType = when (componentType) {
            is ComponentType.Activity -> Ids.commonActivity.constructClassLikeType(arrayOf(ConeStarProjection))
            is ComponentType.Page -> Ids.page.constructClassLikeType(arrayOf(ConeStarProjection))
            is ComponentType.Fragment -> Ids.fragment.constructClassLikeType(arrayOf(ConeStarProjection))
            ComponentType.Others -> return null
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
        val annotatedClassId = owner.classId.outerClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        return when (val componentType = resolveComponentType(annotatedSymbol)) {
            is ComponentType.Activity -> {
                val typeArg = componentType.superTypeRef.unwrapType(0) ?: return null
                val paramType = typeArg as? ConeKotlinType ?: return null
                if (paramType.classId == StandardClassIds.Unit) return null

                val provideParamFunction =
                    createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                        valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)
                    }
                provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
                provideParamFunction.symbol as FirNamedFunctionSymbol
            }

            is ComponentType.Page, ComponentType.Fragment, ComponentType.Others -> null
        }
    }

    private fun generateProvideParamFlowFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val annotatedClassId = owner.classId.outerClassId ?: return null
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

    /**
     * Generates an `override fun inject()` on the annotated activity class. The body is filled in IR.
     *
     * The overridden [Ids.commonActivity] `inject()` is a `@GeneratedByMetroStation` (opt-in) member,
     * so the marker is propagated onto the generated override to satisfy the opt-in requirement.
     */
    private fun generateInjectFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol {
        val injectFunction = createMemberFunction(
            owner,
            Key,
            Ids.injectName,
            session.builtinTypes.unitType.coneType,
        ) {
            status {
                isOverride = true
            }
        }
        injectFunction.replaceAnnotations(listOf(buildSimpleAnnotation(Ids.generatedByMetroStation)))
        return injectFunction.symbol as FirNamedFunctionSymbol
    }

    /**
     * Generates an `override fun injectViewModel(deps[, initialParam])` on the annotated page class,
     * returning the page's ViewModel type. The second `initialParam` parameter is only generated for
     * a [ParamPage][Ids.paramPage]. The body is filled in IR.
     *
     * The overridden Page/ParamPage `injectViewModel` is a `@GeneratedByMetroStation` (opt-in) member,
     * so the marker is propagated onto the generated override to satisfy the opt-in requirement.
     */
    private fun generateInjectViewModelFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val componentType = resolveComponentType(owner)
        if (componentType !is ComponentType.Page) return null

        val viewModelType = resolvePageViewModelType(componentType.superTypeRef, owner, session) as? ConeKotlinType
            ?: return null
        val pageGraphDepsType = Ids.pageGraphDependencies.constructClassLikeType()
        val paramType = if (componentType.hasParam) {
            componentType.superTypeRef.unwrapType(1) as? ConeKotlinType ?: return null
        } else {
            null
        }

        val injectViewModelFunction = createMemberFunction(
            owner,
            Key,
            Ids.injectViewModelName,
            viewModelType,
        ) {
            status {
                isOverride = true
            }
            valueParameter(Ids.depsName, pageGraphDepsType, key = Key)
            if (paramType != null) {
                valueParameter(Ids.initialParamName, paramType, key = Key)
            }
        }
        injectViewModelFunction.replaceAnnotations(listOf(buildSimpleAnnotation(Ids.generatedByMetroStation)))
        return injectViewModelFunction.symbol as FirNamedFunctionSymbol
    }

    private fun generateFactory(featureGraphOwner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val factoryClassId = featureGraphOwner.classId.createNestedClassId(Ids.nestedFactoryName)
        val factorySymbol = FirRegularClassSymbol(factoryClassId)
        val ownerClassId = featureGraphOwner.classId.outerClassId!!
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

            superTypeRefs += buildResolvedTypeRef {
                val rootType = ownerClassId.constructClassLikeType()
                val serviceProviderType =
                    ownerClassId.createNestedClassId(Ids.featureServiceProviderName).constructClassLikeType()
                val graphType = featureGraphOwner.classId.constructClassLikeType()

                val extraDependenciesType = owner.resolveExtraDependencies()

                val componentType = resolveComponentType(owner)
                coneType = if (componentType is ComponentType.Page) {
                    // PageGraphFactory<Feature, VM, Param, ServiceProvider, ExtraDependencies, Graph>
                    val viewModelType = resolvePageViewModelType(componentType.superTypeRef, owner, session)
                    val paramType: ConeTypeProjection = if (componentType.hasParam) {
                        componentType.superTypeRef.unwrapType(1) ?: StandardClassIds.Unit.constructClassLikeType()
                    } else {
                        StandardClassIds.Unit.constructClassLikeType()
                    }
                    Ids.pageGraphFactory.constructClassLikeType(
                        arrayOf(
                            rootType,
                            viewModelType,
                            paramType,
                            serviceProviderType,
                            extraDependenciesType,
                            graphType,
                        )
                    )
                } else {
                    // GraphFactory<Root, ServiceProvider, ExtraDependencies, Graph>
                    Ids.graphFactory.constructClassLikeType(
                        arrayOf(
                            rootType,
                            serviceProviderType,
                            extraDependenciesType,
                            graphType,
                        )
                    )
                }
            }
        }
        return factory.symbol
    }

    /**
     * Resolves the extraDependencies type from the @MetroStation annotation.
     * Returns EmptyExtraDependencies if extraDependencies is Nothing::class.
     */
    private fun FirClassSymbol<*>.resolveExtraDependencies(): ConeKotlinType {
        val annotation = getAnnotationByClassId(Ids.metroStation, session)
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
            with(typeResolverFactory.create(owner)!!) {
                owner.deepResolveSuperType(Ids.commonActivity, session)
                    ?.let { return@getOrPut ComponentType.Activity(it) }

                owner.deepResolveSuperType(Ids.paramPage, session)
                    ?.let { return@getOrPut ComponentType.Page(it, hasParam = true) }

                owner.deepResolveSuperType(Ids.page, session)
                    ?.let { return@getOrPut ComponentType.Page(it, hasParam = false) }

                owner.deepResolveSuperType(Ids.commonFragment, session)
                    ?.let { return@getOrPut ComponentType.Fragment }

                owner.deepResolveSuperType(Ids.commonDialogFragment, session)
                    ?.let { return@getOrPut ComponentType.Fragment }
            }
            ComponentType.Others
        }
    }


    /**
     * Validates that the annotated class has the required @MetroStation annotation and
     * retrieves the appDependencies type from it.
     */
    private fun FirClassSymbol<*>.requireAppDependencies(): ConeKotlinType {
        val annotation = getAnnotationByClassId(Ids.metroStation, session)
            ?: error("Cannot find @MetroStation annotation")

        // Try resolved argument mapping first, then fall back to argument list
        val rawExpr = annotation.argumentMapping.mapping[Ids.appDependenciesName]
            ?: (annotation as? FirAnnotationCall)?.argumentList?.arguments
                ?.filterIsInstance<FirNamedArgumentExpression>()
                ?.find { it.name == Ids.appDependenciesName }
            ?: error("Cannot find @MetroStation.appDependencies argument")

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

    override fun getContributionTargets(): List<ContributionTarget> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(Ids.metroStationPredicate)
            .filterIsInstance<FirRegularClassSymbol>()
            .map { classSymbol ->
                val serviceProvider = classSymbol.classId.createNestedClassId(Ids.featureServiceProviderName)
                ContributionTarget(contributingClassId = serviceProvider, scope = ClassIds.appScope)
            }
    }

    internal object Key : GeneratedDeclarationKey()

    public class Factory : MetroFirDeclarationGenerationExtension.Factory {
        override fun create(
            session: FirSession,
            options: MetroOptions,
            compatContext: CompatContext,
        ): MetroFirDeclarationGenerationExtension = MetroStationFir(session, compatContext)
    }
}
