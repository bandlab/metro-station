package com.bandlab.metro.extensions.component

import com.bandlab.metro.extensions.component.ContributesComponentIds
import com.bandlab.metro.extensions.utils.ClassIds
import com.bandlab.metro.extensions.utils.buildSimpleAnnotation
import com.bandlab.metro.extensions.utils.getClassCall
import com.bandlab.metro.extensions.utils.markAbstract
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
import org.jetbrains.kotlin.fir.declarations.hasAnnotationWithClassId
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.ConeKotlinType
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
 *   @DependencyGraph(MyActivity::class)
 *   interface FeatureGraph {
 *     fun inject(feature: MyActivity)
 *
 *     // If the param type in CommonActivity<T> isn't `Unit`
 *     @Provides
 *     fun provideParam(feature: MyActivity): MyActivity.Param = feature.param
 *
 *     interface Factory {
 *       fun create(
 *         @Provides feature: MyActivity,
 *         @Includes serviceProvider: FeatureServiceProvider
 *       ): FeatureGraph
 *     }
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
public class ContributesComponentFir(session: FirSession) : MetroFirDeclarationGenerationExtension(session) {

    private val predicate = Ids.predicate

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        return when {
            classSymbol.hasAnnotationWithClassId(Ids.contributesComponent, session) ->
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
            owner.hasAnnotationWithClassId(Ids.contributesComponent, session)
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
            owner.hasAnnotationWithClassId(Ids.contributesComponent, session)
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
        // For our generated Factory, expose the "create" function
        if (classSymbol.classId.shortClassName == Ids.nestedFactoryName) {
            return setOf(Ids.createName)
        }
        // For our generated FeatureGraph, expose the "inject" function
        if (classSymbol.classId.shortClassName == Ids.graphName) {
            return setOf(Ids.injectName, Ids.provideParamName)
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

        // Generate "create" function for Factory
        if (callableId.callableName == Ids.createName &&
            ownerClassId.shortClassName == Ids.nestedFactoryName
        ) {
            return listOf(generateCreateFunction(ownerClassId, owner))
        }

        // Generate "inject" function for FeatureGraph
        if (callableId.callableName == Ids.injectName &&
            ownerClassId.shortClassName == Ids.graphName
        ) {
            // The annotated class is the parent of this Graph
            val annotatedClassId = owner.classId.parentClassId ?: return emptyList()
            val annotatedSymbol =
                session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                    ?: return emptyList()
            val annotatedType = annotatedSymbol.defaultType()

            val injectFunction =
                createMemberFunction(owner, Key, Ids.injectName, session.builtinTypes.unitType.coneType) {
                    valueParameter(Ids.featureName, annotatedType, key = Key)
                }
            injectFunction.markAbstract(owner)

            return listOf(injectFunction.symbol)
        }

        // Generate "provideParam" function for FeatureGraph
        if (callableId.callableName == Ids.provideParamName &&
            ownerClassId.shortClassName == Ids.graphName
        ) {
            val annotatedClassId = owner.classId.parentClassId ?: return emptyList()
            val annotatedSymbol =
                session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as? FirRegularClassSymbol
                    ?: return emptyList()

            //TODO: Handle Page as well
            val superType = annotatedSymbol.resolvedSuperTypes
                .find { it.classId == Ids.commonActivity }
                ?: return emptyList()

            val typeArg = superType.typeArguments.firstOrNull() ?: return emptyList()
            val paramType = typeArg as? ConeKotlinType ?: return emptyList()

            if (paramType.classId == StandardClassIds.Unit) return emptyList()

            val provideParamFunction =
                createMemberFunction(owner, Key, Ids.provideParamName, paramType) {
                    valueParameter(Ids.featureName, annotatedSymbol.defaultType(), key = Key)
                }
            provideParamFunction.replaceAnnotations(listOf(buildSimpleAnnotation(ClassIds.provides)))

            return listOf(provideParamFunction.symbol)
        }

        return emptyList()
    }

    private fun generateCreateFunction(
        factoryClassId: ClassId,
        factorySymbol: FirClassSymbol<*>,
    ): FirNamedFunctionSymbol {
        val featureGraphClassId = factoryClassId.outerClassId!!
        val annotatedClassId = featureGraphClassId.outerClassId!!
        val annotatedClassSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(annotatedClassId) as FirRegularClassSymbol

        val featureServiceProvider = annotatedClassId.createNestedClassId(Ids.featureServiceProviderName)
        val createFunction = createMemberFunction(
            factorySymbol,
            Key,
            Ids.createName,
            featureGraphClassId.constructClassLikeType(),
        ) {
            valueParameter(Ids.featureName, annotatedClassSymbol.defaultType(), key = Key)
            valueParameter(Ids.serviceProviderName, featureServiceProvider.constructClassLikeType(), key = Key)
        }
        createFunction.markAbstract(factorySymbol)

        // Add @Provides to root parameter
        createFunction.valueParameters[0].replaceAnnotations(
            listOf(buildSimpleAnnotation(ClassIds.provides))
        )
        // Add @Includes to serviceProvider parameter
        createFunction.valueParameters[1].replaceAnnotations(
            listOf(buildSimpleAnnotation(ClassIds.includes))
        )

        return createFunction.symbol
    }

    private fun generateFeatureGraph(owner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val nestedClassId = owner.classId.createNestedClassId(Ids.graphName)
        val classSymbol = FirRegularClassSymbol(nestedClassId)
        val ownerSymbol = owner as FirRegularClassSymbol

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

            // Metro's FIR cannot see generated providers inside the graph, so we'll need to annotate the graph
            // with @IROnlyFactoties to generate factories in IR.
            annotations += buildSimpleAnnotation(ClassIds.irOnlyFactories)

            //TODO Scope the feature based on their type
            annotations += buildSimpleAnnotation(Ids.activityScope)

            // @DependencyGraph(MyFeature::class) annotation
            //TODO: Support custom graphMarker
            annotations += buildSimpleAnnotation(
                classId = ClassIds.dependencyGraph,
                argumentMapping = buildAnnotationArgumentMapping {
                    mapping[ClassIds.scopeName] = ownerSymbol.getClassCall()
                }
            )
        }
        return featureGraph.symbol
    }

    private fun generateFactory(featureGraphOwner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val factoryClassId = featureGraphOwner.classId.createNestedClassId(Ids.nestedFactoryName)
        val factorySymbol = FirRegularClassSymbol(factoryClassId)

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
            superTypeRefs += session.builtinTypes.anyType
            annotations += buildSimpleAnnotation(ClassIds.dependencyGraphFactory)
        }
        return factory.symbol
    }

    private fun generateFeatureServiceProvider(owner: FirClassSymbol<*>): FirClassLikeSymbol<*> {
        val nestedClassId = owner.classId.createNestedClassId(Ids.featureServiceProviderName)
        val classSymbol = FirRegularClassSymbol(nestedClassId)

        //TODO: Add required supertypes based on the annotated class
        val commonActivitySPSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(Ids.commonActivityServiceProvider)
        val defaultScreenSPSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(Ids.defaultScreenServiceProvider)

        val appScopeSymbol =
            session.symbolProvider.getClassLikeSymbolByClassId(ClassIds.appScope) as FirRegularClassSymbol

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
            if (commonActivitySPSymbol != null) {
                superTypeRefs += buildResolvedTypeRef {
                    coneType = Ids.commonActivityServiceProvider.constructClassLikeType()
                }
            }

            // Add DefaultScreenServiceProvider if available
            if (defaultScreenSPSymbol != null) {
                superTypeRefs += buildResolvedTypeRef {
                    coneType = Ids.defaultScreenServiceProvider.constructClassLikeType()
                }
            }

            // If no supertypes were added, use Any
            if (superTypeRefs.isEmpty()) {
                superTypeRefs += session.builtinTypes.anyType
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
                val classId = extractClassIdFromExpression(argument, this.classId.packageFqName)
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
    private fun extractClassIdFromExpression(expr: FirExpression, ownerPackage: FqName): ClassId? {
        val names = mutableListOf<String>()
        var current: FirExpression? = expr
        while (current is FirPropertyAccessExpression) {
            val ref = current.calleeReference
            names.add(0, ref.name.asString())
            current = current.explicitReceiver
        }
        if (names.isEmpty()) return null

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
        return null
    }

    override fun getContributionHints(): List<ContributionHint> {
        return session.predicateBasedProvider
            .getSymbolsByPredicate(predicate)
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
        ): MetroFirDeclarationGenerationExtension = ContributesComponentFir(session)
    }
}
