package com.bandlab.metro.extensions.component

import com.bandlab.metro.extensions.component.ContributesComponentIds
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
import org.jetbrains.kotlin.fir.expressions.*
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
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.*
import com.bandlab.metro.extensions.component.ContributesComponentIds as Ids

/**
 * This FIR declaration generator generates a dependency graph for the feature that is annotated with
 * [ContributesComponentIds.contributesComponentFqName].
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
 *     bindingContainers = [DefaultActivityDeps::class]
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

    override val enableFirInIde: Boolean
        get() = true

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Ids.predicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        return when {
            session.predicateBasedProvider.matches(Ids.predicate, classSymbol) ->
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
            session.predicateBasedProvider.matches(Ids.predicate, owner)
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
            session.predicateBasedProvider.matches(Ids.predicate, owner)
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

        val commonActivitySuperTypeRef = owner.findSuperTypeRef(Ids.commonActivity)
        val pageSuperTypeRef = owner.findSuperTypeRef(Ids.paramPage) ?: owner.findSuperTypeRef(Ids.page)

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

            if (commonActivitySuperTypeRef != null) {
                annotations += buildSimpleAnnotation(Ids.activityScope)
                superTypeRefs += buildResolvedTypeRef {
                    coneType = Ids.membersInjectorProvider.constructClassLikeType(
                        arrayOf(owner.defaultType())
                    )
                }
            }

            if (pageSuperTypeRef != null) {
                annotations += buildSimpleAnnotation(Ids.pageScope)
                superTypeRefs += buildResolvedTypeRef {
                    val viewModelType = pageSuperTypeRef.unwrapType()!!
                    coneType = Ids.pageInjector.constructClassLikeType(arrayOf(viewModelType))
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
                            val defaultDepsId = when {
                                commonActivitySuperTypeRef != null -> Ids.defaultActivityDeps
                                pageSuperTypeRef != null -> Ids.defaultPageDependencies
                                else -> error("Unsupported Component Type")
                            }

                            session.symbolProvider.getClassLikeSymbolByClassId(defaultDepsId)?.let {
                                arguments += it.getClassCall()
                            }

                            if (pageSuperTypeRef != null) {
                                session.symbolProvider.getClassLikeSymbolByClassId(Ids.pageGraphDependenciesModule)
                                    ?.let { arguments += it.getClassCall() }
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

    //TODO: Forbid primitive param types
    private fun generateProvideParamFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val annotatedClassId = owner.classId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        val commonActivitySuperType = annotatedSymbol.resolvedSuperTypes.find { it.classId == Ids.commonActivity }
        val paramPageSuperType = annotatedSymbol.resolvedSuperTypes.find { it.classId == Ids.paramPage }

        return when {
            commonActivitySuperType != null -> {
                val typeArg = commonActivitySuperType.typeArguments.firstOrNull() ?: return null
                val paramType = typeArg as? ConeKotlinType ?: return null
                if (paramType.classId == StandardClassIds.Unit) return null

                val provideParamFunction =
                    createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                        valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)
                    }
                provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
                provideParamFunction.symbol as FirNamedFunctionSymbol
            }

            paramPageSuperType != null -> {
                // ParamPage<ViewModel, Param> - extract the 2nd type arg (Param)
                val paramTypeArg = paramPageSuperType.typeArguments.getOrNull(1) ?: return null
                val paramType = paramTypeArg as? ConeKotlinType ?: return null

                val pageGraphDepsType = Ids.pageGraphDependencies.constructClassLikeType()
                val provideParamFunction =
                    createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                        valueParameter("pageGraphDependencies".asName(), pageGraphDepsType, key = Key)
                    }
                provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
                provideParamFunction.symbol as FirNamedFunctionSymbol
            }

            else -> null
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
            superTypeRefs += buildResolvedTypeRef {
                val rootType = ownerClassId.constructClassLikeType()
                val serviceProviderType =
                    ownerClassId.createNestedClassId(Ids.featureServiceProviderName).constructClassLikeType()
                val graphType = featureGraphOwner.classId.constructClassLikeType()

                val baseFactoryType = when {
                    owner.findSuperTypeRef(Ids.commonActivity) != null -> Ids.graphFactory
                    owner.findSuperTypeRef(Ids.page) != null ||
                        owner.findSuperTypeRef(Ids.paramPage) != null -> Ids.pageGraphFactory

                    else -> error("Unsupported Component Type")
                }

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
            else -> extractClassIdFromExpression(argument, this.classId)
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

        val commonActivitySuperTypeRef = owner.findSuperTypeRef(Ids.commonActivity)

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

            // Add CommonActivity.ServiceProvider if available
            if (commonActivitySuperTypeRef != null) {
                superTypeRefs += buildResolvedTypeRef {
                    coneType = Ids.commonActivityServiceProvider.constructClassLikeType()
                }
            }

            // Add DefaultScreenServiceProvider if available
            superTypeRefs += buildResolvedTypeRef {
                coneType = Ids.defaultScreenServiceProvider.constructClassLikeType()
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

    /**
     * Validates that the annotated class has the required @ContributesComponent annotation and
     * retrieves the appDependencies type from it.
     *
     * TODO: Quite delicate function, might extract this to util if it's used elsewhere.
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
                val classId = extractClassIdFromExpression(argument, this.classId)
                    ?: error("Cannot extract ClassId from GetClassCall argument: ${argument::class.simpleName}")
                classId.constructClassLikeType()
            }
        }
    }

    /**
     * Recursively extract a [ClassId] from an unresolved property access expression chain like
     * `MyActivity.ServiceProvider`. Walks the receiver chain to collect name segments and resolves
     * them against the symbol provider.
     */
    private fun extractClassIdFromExpression(expr: FirExpression, ownerClassId: ClassId): ClassId? {
        val names = mutableListOf<String>()
        var current: FirExpression? = expr
        while (current is FirPropertyAccessExpression) {
            val ref = current.calleeReference
            names.add(0, ref.name.asString())
            current = current.explicitReceiver
        }
        if (names.isEmpty()) return null

        val ownerPackage = ownerClassId.packageFqName

        // Try as a nested class of the owner first
        var nestedClassId = ownerClassId
        for (name in names) {
            nestedClassId = nestedClassId.createNestedClassId(Name.identifier(name))
        }
        if (session.symbolProvider.getClassLikeSymbolByClassId(nestedClassId) != null) {
            return nestedClassId
        }

        // Try progressively: first name could be a top-level class, then nested classes
        // Also try as a package prefix
        for (pkgSplit in names.indices) {
            val packageFqName = if (pkgSplit == 0) {
                // Try with no package (use the owner's package)
                ownerPackage
            } else {
                FqName.fromSegments(names.subList(0, pkgSplit))
            }
            val classNames = if (pkgSplit == 0) names else names.subList(pkgSplit, names.size)
            if (classNames.isEmpty()) continue

            var classId = ClassId(packageFqName, Name.identifier(classNames[0]))
            if (session.symbolProvider.getClassLikeSymbolByClassId(classId) == null) continue

            for (i in 1 until classNames.size) {
                classId = classId.createNestedClassId(Name.identifier(classNames[i]))
            }
            if (session.symbolProvider.getClassLikeSymbolByClassId(classId) != null) {
                return classId
            }
        }

        // Fallback: If we can't resolve it, just assume the first name is a class in the owner's package
        // or a completely unresolved name. This prevents an IDE crash and lets the compiler 
        // report an unresolved reference later if it's truly invalid.
        var fallbackClassId = ClassId(ownerPackage, Name.identifier(names[0]))
        for (i in 1 until names.size) {
            fallbackClassId = fallbackClassId.createNestedClassId(Name.identifier(names[i]))
        }
        return fallbackClassId
    }

    override fun getContributionHints(): List<ContributionHint> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(Ids.predicate)
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
