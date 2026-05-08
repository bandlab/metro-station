package com.bandlab.metro.extensions.injector

import com.bandlab.metro.extensions.utils.*
import com.fueledbycaffeine.autoservice.AutoService
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
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildCollectionLiteral
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.*
import com.bandlab.metro.extensions.component.ContributesComponentIds as Ids

/**
 * This FIR declaration generator generates a graph extension for the feature that is annotated with
 * [Ids.contributesInjector].
 *
 * ```kotlin
 * @ContributesInjector
 * class MyActivity : CommonActivity<Param>() {
 *
 *   data class Param(val int: Int)
 *
 *   // This extension generates:
 *   @IROnlyFactories
 *   @ActivityScope
 *   @GraphExtension(
 *     scope = MyActivity::class,
 *     bindingContainers = [DefaultActivityDependencies::class, FeatureBindings::class]
 *   )
 *   interface FeatureExtension : MembersInjectorProvider<MyActivity> {
 *
 *     @ContributesTo(AppScope::class)
 *     @GraphExtension.Factory
 *     interface Factory {
 *       fun create(@Provides feature: MyActivity): FeatureExtension
 *     }
 *   }
 *
 *   @IROnlyFactories
 *   @BindingContainer
 *   object FeatureBindings {
 *     @Provides
 *     fun provideBaseType(feature: MyActivity): CommonActivity<*> = feature
 *
 *     // If the feature has a param
 *     @Provides
 *     fun provideParam(feature: MyActivity): MyActivity.Param = feature.param
 *   }
 *
 *   @IROnlyFactories
 *   @ContributesTo(scope = AppScope::class)
 *   interface ExtensionFactoryContribution {
 *     @Provides
 *     @IntoMap
 *     @ClassKey(MyActivity::class)
 *     fun provideFactory(factory: FeatureExtension.Factory): Any = factory
 *   }
 * }
 * ```
 */
public class ContributesInjectorFir(session: FirSession, compatContext: CompatContext) :
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
    }

    private val componentTypeCache = mutableMapOf<ClassId, ComponentType>()

    @Suppress("INVISIBLE_REFERENCE")
    private val typeResolverFactory by lazy { MetroFirTypeResolver.Factory(session) }

    //TODO: Enable FIR in IDE support
    override val enableFirInIde: Boolean
        get() = false

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Ids.injectorPredicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        return when {
            session.predicateBasedProvider.matches(Ids.injectorPredicate, classSymbol) ->
                setOf(Ids.featureExtensionName, Ids.extensionFactoryContributionName, Ids.featureBindingsName)

            classSymbol.origin == Key.origin && classSymbol.classId.shortClassName == Ids.featureExtensionName ->
                setOf(Ids.nestedFactoryName)

            else -> emptySet()
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        // Generate FeatureExtension inside the annotated class
        if (name == Ids.featureExtensionName &&
            session.predicateBasedProvider.matches(Ids.injectorPredicate, owner)
        ) {
            return generateFeatureExtension(owner)
        }
        // Generate FeatureBindings inside the annotated class
        if (name == Ids.featureBindingsName &&
            session.predicateBasedProvider.matches(Ids.injectorPredicate, owner)
        ) {
            return generateFeatureBindings(owner)
        }
        // Generate Factory inside FeatureExtension
        if (name == Ids.nestedFactoryName &&
            owner.origin == Key.origin &&
            owner.classId.shortClassName == Ids.featureExtensionName
        ) {
            return generateFactory(owner)
        }
        // Generate ExtensionFactoryContribution inside the annotated class
        if (name == Ids.extensionFactoryContributionName &&
            session.predicateBasedProvider.matches(Ids.injectorPredicate, owner)
        ) {
            return generateExtensionFactoryContribution(owner)
        }
        return null
    }

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> {
        if (classSymbol.origin != Key.origin) return emptySet()
        if (classSymbol.classId.shortClassName == Ids.featureBindingsName) {
            return setOf(SpecialNames.INIT, Ids.provideBaseTypeName, Ids.provideParamName, Ids.provideParamFlowName)
        }
        if (classSymbol.classId.shortClassName == Ids.nestedFactoryName) {
            return setOf(Ids.createName)
        }
        if (classSymbol.classId.shortClassName == Ids.extensionFactoryContributionName) {
            return setOf(Ids.provideFactoryName)
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

        // Generate "provideBaseType" for FeatureBindings
        if (callableId.callableName == Ids.provideBaseTypeName &&
            ownerClassId.shortClassName == Ids.featureBindingsName
        ) {
            val fn = generateProvideBaseTypeFunction(owner) ?: return emptyList()
            return listOf(fn)
        }

        // Generate "provideParam" for FeatureBindings
        if (callableId.callableName == Ids.provideParamName &&
            ownerClassId.shortClassName == Ids.featureBindingsName
        ) {
            val fn = generateProvideParamFunction(owner) ?: return emptyList()
            return listOf(fn)
        }

        // Generate "provideParamFlow" for FeatureBindings (ParamPage only)
        if (callableId.callableName == Ids.provideParamFlowName &&
            ownerClassId.shortClassName == Ids.featureBindingsName
        ) {
            val fn = generateProvideParamFlowFunction(owner) ?: return emptyList()
            return listOf(fn)
        }

        // Generate "create" for Factory
        if (callableId.callableName == Ids.createName &&
            ownerClassId.shortClassName == Ids.nestedFactoryName
        ) {
            val fn = generateCreateFunction(owner) ?: return emptyList()
            return listOf(fn)
        }

        // Generate "provideFactory" for ExtensionFactoryContribution
        if (callableId.callableName == Ids.provideFactoryName &&
            ownerClassId.shortClassName == Ids.extensionFactoryContributionName
        ) {
            val fn = generateProvideFactoryFunction(owner) ?: return emptyList()
            return listOf(fn)
        }

        return emptyList()
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val owner = context.owner
        if (owner.origin != Key.origin) return emptyList()
        if (owner.classId.shortClassName == Ids.featureBindingsName) {
            return listOf(createDefaultPrivateConstructor(owner, Key).symbol)
        }
        return emptyList()
    }

    private fun generateFeatureExtension(owner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val nestedClassId = owner.classId.createNestedClassId(Ids.featureExtensionName)
        val classSymbol = FirRegularClassSymbol(nestedClassId)
        val annotation = owner.getAnnotationByClassId(Ids.contributesInjector, session) as? FirAnnotationCall

        val componentType = resolveComponentType(owner)
        val componentScope = when (componentType) {
            is ComponentType.Activity -> Ids.activityScope
            is ComponentType.Page -> Ids.pageScope
            ComponentType.Fragment -> Ids.fragmentScope
        }

        val featureExtension = buildRegularClass {
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

            annotations += buildSimpleAnnotation(ClassIds.irOnlyFactories)

            // @GraphExtension annotation
            annotations += buildSimpleAnnotation(
                classId = ClassIds.graphExtension,
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
                            }

                            defaultDependenciesIds.forEach { dependenciesId ->
                                session.symbolProvider.getClassLikeSymbolByClassId(dependenciesId)?.let {
                                    arguments += it.getClassCall()
                                }
                            }

                            // Include the generated FeatureBindings binding container
                            val featureBindingsClassId = owner.classId.createNestedClassId(Ids.featureBindingsName)
                            session.symbolProvider.getClassLikeSymbolByClassId(featureBindingsClassId)?.let {
                                arguments += it.getClassCall()
                            }
                        }
                    }
                }
            )
        }
        return featureExtension.symbol
    }

    /**
     * Important! FeatureBindings needs to be a separate type than the graph extension, this seems to be the only
     * way for metro to process those bindings correctly for cross-module compilation.
     */
    private fun generateFeatureBindings(featureExtensionOwner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val bindingsClassId = featureExtensionOwner.classId.createNestedClassId(Ids.featureBindingsName)
        val classSymbol = FirRegularClassSymbol(bindingsClassId)

        val featureBindings = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = featureExtensionOwner.source
            classKind = ClassKind.OBJECT
            scopeProvider = session.kotlinScopeProvider
            this.name = bindingsClassId.shortClassName
            symbol = classSymbol
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.FINAL,
                Visibilities.Public.toEffectiveVisibility(featureExtensionOwner, forClass = true),
            )

            annotations += buildSimpleAnnotation(ClassIds.bindingContainer)
            annotations += buildSimpleAnnotation(ClassIds.irOnlyFactories)
        }
        return featureBindings.symbol
    }

    private fun generateFactory(featureExtensionOwner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val factoryClassId = featureExtensionOwner.classId.createNestedClassId(Ids.nestedFactoryName)
        val factorySymbol = FirRegularClassSymbol(factoryClassId)

        val annotatedClassId = featureExtensionOwner.classId.parentClassId!!
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as FirRegularClassSymbol
        val parentScopeId = resolveParentScopeClassId(annotatedSymbol)

        val factory = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = featureExtensionOwner.source
            classKind = ClassKind.INTERFACE
            scopeProvider = session.kotlinScopeProvider
            this.name = factoryClassId.shortClassName
            symbol = factorySymbol
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(featureExtensionOwner, forClass = true),
            )

            // @ContributesTo(scope)
            annotations += buildSimpleAnnotation(
                classId = ClassIds.contributesTo,
                argumentMapping = buildAnnotationArgumentMapping {
                    mapping[ClassIds.scopeName] = session.symbolProvider
                        .getClassLikeSymbolByClassId(parentScopeId)!!
                        .getClassCall()
                }
            )

            // @GraphExtension.Factory
            annotations += buildSimpleAnnotation(ClassIds.graphExtensionFactory)
        }
        return factory.symbol
    }

    private fun generateExtensionFactoryContribution(owner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val nestedClassId = owner.classId.createNestedClassId(Ids.extensionFactoryContributionName)
        val classSymbol = FirRegularClassSymbol(nestedClassId)
        val parentScopeId = resolveParentScopeClassId(owner)

        val contribution = buildRegularClass {
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

            annotations += buildSimpleAnnotation(ClassIds.irOnlyFactories)

            // @ContributesTo(scope)
            annotations += buildSimpleAnnotation(
                classId = ClassIds.contributesTo,
                argumentMapping = buildAnnotationArgumentMapping {
                    mapping[ClassIds.scopeName] = session.symbolProvider
                        .getClassLikeSymbolByClassId(parentScopeId)!!
                        .getClassCall()
                }
            )
        }
        return contribution.symbol
    }

    private fun generateProvideBaseTypeFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val annotatedClassId = owner.classId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        val componentType = resolveComponentType(annotatedSymbol)
        val baseType = when (componentType) {
            is ComponentType.Activity -> Ids.commonActivity.constructClassLikeType(arrayOf(ConeStarProjection))
            is ComponentType.Page -> Ids.page.constructClassLikeType(arrayOf(ConeStarProjection))
            ComponentType.Fragment -> Ids.fragment.constructClassLikeType()
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

                val provideParamFunction =
                    createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                        valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)
                    }
                provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
                provideParamFunction.symbol as FirNamedFunctionSymbol
            }

            is ComponentType.Page if componentType.hasParam -> {
                val paramTypeArg = componentType.superTypeRef.unwrapType(1) ?: return null
                val paramType = paramTypeArg as? ConeKotlinType ?: return null

                val pageGraphDepsType = Ids.pageGraphDependencies.constructClassLikeType()
                val provideParamFunction =
                    createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                        valueParameter("pageGraphDependencies".asName(), pageGraphDepsType, key = Key)
                    }
                provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))
                provideParamFunction.symbol as FirNamedFunctionSymbol
            }

            ComponentType.Fragment, is ComponentType.Page -> null
        }
    }

    private fun generateProvideParamFlowFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        val annotatedClassId = owner.classId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        val paramPageSuperTypeRef = annotatedSymbol.findSuperTypeRef(Ids.paramPage)
            ?: return null

        val paramTypeArg = paramPageSuperTypeRef.unwrapType(1) ?: return null
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

    private fun generateCreateFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        // Factory is inside FeatureExtension which is inside the annotated class
        val featureExtensionClassId = owner.classId.parentClassId ?: return null
        val annotatedClassId = featureExtensionClassId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        val featureExtensionType = featureExtensionClassId.constructClassLikeType()

        val componentType = resolveComponentType(annotatedSymbol)
        val createFunction = createMemberFunction(
            owner,
            Key,
            Ids.createName,
            featureExtensionType
        ) {
            valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)

            if (componentType is ComponentType.Page) {
                valueParameter(
                    "pageGraphDependencies".asName(),
                    Ids.pageGraphDependencies.constructClassLikeType(),
                    key = Key
                )
                valueParameter(
                    "navPageDependencies".asName(),
                    Ids.navPageDependencies.constructClassLikeType(),
                    key = Key
                )
            }
        }
        createFunction.markAbstract(owner)

        // Annotate parameters with @Provides or @Includes
        val featureParam = createFunction.valueParameters[0]
        featureParam.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))

        if (componentType is ComponentType.Page) {
            val depsParam = createFunction.valueParameters[1]
            depsParam.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))

            val navDepsParam = createFunction.valueParameters[2]
            navDepsParam.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.includes)))
        }

        return createFunction.symbol as FirNamedFunctionSymbol
    }

    private fun generateProvideFactoryFunction(owner: FirClassSymbol<*>): FirNamedFunctionSymbol? {
        // ExtensionFactoryContribution is inside the annotated class
        val annotatedClassId = owner.classId.parentClassId ?: return null
        val annotatedSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                ?: return null

        // The parameter type is FeatureExtension.Factory
        val factoryClassId = annotatedClassId
            .createNestedClassId(Ids.featureExtensionName)
            .createNestedClassId(Ids.nestedFactoryName)
        val factoryType = factoryClassId.constructClassLikeType()

        // Return type is Any
        val returnType = session.builtinTypes.anyType.coneType

        val provideFactoryFunction = createMemberFunction(
            owner,
            Key,
            Ids.provideFactoryName,
            returnType
        ) {
            valueParameter(Ids.factoryParamName, factoryType, key = Key)
        }

        provideFactoryFunction.replaceAnnotations(
            listOf(
                buildSimpleAnnotation(ClassIds.provides),
                buildSimpleAnnotation(ClassIds.intoMap),
                buildSimpleAnnotation(
                    classId = ClassIds.classKey,
                    argumentMapping = buildAnnotationArgumentMapping {
                        mapping[ClassIds.valueName] = annotatedSymbol.getClassCall()
                    }
                ),
            )
        )

        return provideFactoryFunction.symbol as FirNamedFunctionSymbol
    }

    /**
     * Resolves the parent scope ClassId from the @ContributesInjector(scope = ...) annotation.
     * Defaults to AppScope if not specified.
     */
    private fun resolveParentScopeClassId(owner: FirClassSymbol<*>): ClassId {
        return resolveScopeClassIdFromAnnotation(owner, Ids.contributesInjector, session, ClassIds.appScope)
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

                owner.deepResolveSuperType(Ids.fragmentBaseType, session)
                    ?.let { return@getOrPut ComponentType.Fragment }

                owner.deepResolveSuperType(Ids.dialogFragmentBaseType, session)
                    ?.let { return@getOrPut ComponentType.Fragment }
            }
            error(
                "Cannot resolve component type for ${owner.classId}. " +
                    "Class must extend one of: CommonActivity, CommonFragment, Page, or ParamPage"
            )
        }
    }

    override fun getContributionHints(): List<ContributionHint> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(Ids.injectorPredicate)
            .filterIsInstance<FirRegularClassSymbol>()
            .flatMap { classSymbol ->
                val parentScope = resolveParentScopeClassId(classSymbol)
                val contributionClassId = classSymbol.classId.createNestedClassId(Ids.extensionFactoryContributionName)
                val factoryClassId = classSymbol.classId
                    .createNestedClassId(Ids.featureExtensionName)
                    .createNestedClassId(Ids.nestedFactoryName)
                listOf(
                    ContributionHint(contributingClassId = contributionClassId, scope = parentScope),
                    ContributionHint(contributingClassId = factoryClassId, scope = parentScope),
                )
            }
    }

    internal object Key : GeneratedDeclarationKey()

    @AutoService
    public class Factory : MetroFirDeclarationGenerationExtension.Factory {
        override fun create(
            session: FirSession,
            options: MetroOptions,
            compatContext: CompatContext,
        ): MetroFirDeclarationGenerationExtension = ContributesInjectorFir(session, compatContext)
    }
}
