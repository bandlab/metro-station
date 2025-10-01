package com.bandlab.metro.station.fir

import com.bandlab.metro.station.Predicates
import com.bandlab.metro.station.Symbols.ClassIds
import com.bandlab.metro.station.Symbols.Names
import com.bandlab.metro.station.plus
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameterCopy
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension.TypeResolveService
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

/**
 * Generates a Metro GraphExtension interface for each class annotated with @StationEntry:
 *
 * ```kotlin
 * @StationEntry(parentScope = AppScope::class)
 * class MyActivity
 *
 * // Generated FIR structure:
 * @GraphExtension(scope = MyActivity::class)
 * interface MyActivityGraphExtension {
 *   fun inject(target: MyActivity)
 *
 *   @ContributesTo(AppScope::class)
 *   @GraphExtension.Factory
 *   interface Factory {
 *     fun create(@Provides target: MyActivity): MyActivityGraphExtension
 *   }
 * }
 * ```
 */
internal class StationEntryGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    // FIR cache for graph extension class ids to original class symbols.
    private val symbols: FirCache<Unit, Map<ClassId, FirRegularClassSymbol>, TypeResolveService?> =
        session.firCachesFactory.createCache { _, _ ->
            session.predicateBasedProvider
                .getSymbolsByPredicate(Predicates.stationEntry)
                .filterIsInstance<FirRegularClassSymbol>()
                .associateBy {
                    ClassId(
                        packageFqName = it.packageFqName(),
                        topLevelName = it.name + Names.GraphExtensionClass
                    )
                }
        }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Predicates.stationEntry)
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelClassIds(): Set<ClassId> {
        return symbols.getValue(Unit).keys
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        val originalSymbol = symbols.getValue(Unit)[classId] ?: return null
        val stationEntryAnnotation =
            originalSymbol.getAnnotationByClassId(ClassIds.StationEntry, session) ?: return null

        val graphExtension = createTopLevelClass(classId, Key, classKind = ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }

        val graphExtensionAnnotation = buildAnnotation {
            //TODO: Implement scoping

            annotationTypeRef = ClassIds.GraphExtension.firTypeRef()

            val stationEntryArguments = stationEntryAnnotation.argumentMapping.mapping
            argumentMapping = buildAnnotationArgumentMapping {
                val scopeArgument = stationEntryArguments[Names.ScopeParam]
                // Check if the provided scope is Nothing
                val scopeArgumentType = scopeArgument?.resolvedType?.typeArguments?.single()?.type
                val isScopeNothing = scopeArgumentType == null || scopeArgumentType.classId == StandardClassIds.Nothing

                mapping[Names.ScopeParam] = if (isScopeNothing) {
                    originalSymbol.getClassCall()
                } else {
                    scopeArgument
                }

                // Port params into metro's GraphExtension
                stationEntryArguments[Names.AdditionalScopesParam]?.let { mapping[Names.AdditionalScopesParam] = it }
                stationEntryArguments[Names.ExcludesParam]?.let { mapping[Names.ExcludesParam] = it }
                stationEntryArguments[Names.BindingContainersParam]?.let { mapping[Names.BindingContainersParam] = it }
            }
        }
        graphExtension.replaceAnnotations(
            listOf(graphExtensionAnnotation)
        )

        return graphExtension.symbol
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext
    ): Set<Name> {
        return if (classSymbol.classId in symbols.getValue(Unit)) {
            setOf(Names.FactoryClass)
        } else {
            emptySet()
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        if (name != Names.FactoryClass) return null

        val originalSymbol = symbols.getValue(Unit)[owner.classId] ?: return null
        val stationEntryAnnotation =
            originalSymbol.getAnnotationByClassId(ClassIds.StationEntry, session) ?: return null
        val parentScope = stationEntryAnnotation.argumentMapping.mapping[Names.ParentScopeParam]
            ?: return null

        val factory = createNestedClass(owner, name, Key, classKind = ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }

        val contributesToAnnotation = buildAnnotation {
            annotationTypeRef = ClassIds.ContributesTo.firTypeRef()
            argumentMapping = buildAnnotationArgumentMapping {
                mapping[Names.ScopeParam] = parentScope
            }
        }

        factory.replaceAnnotations(
            listOf(
                contributesToAnnotation,
                ClassIds.GraphExtensionFactory.firAnnotation()
            )
        )

        return factory.symbol
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val outerClassId = classSymbol.classId.outerClassId
        return when {
            classSymbol.classId in symbols.getValue(Unit) -> {
                setOf(Names.InjectMethod)
            }

            classSymbol.classId.shortClassName == Names.FactoryClass && outerClassId in symbols.getValue(Unit) -> {
                setOf(Names.CreateMethod)
            }

            else -> emptySet()
        }
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()

        when (callableId.callableName) {
            Names.InjectMethod -> {
                val originalSymbol = symbols.getValue(Unit)[owner.classId] ?: return emptyList()
                val injectFunction = createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = session.builtinTypes.unitType.coneType
                ) {
                    modality = Modality.ABSTRACT
                    valueParameter(
                        name = Names.TargetParam,
                        type = originalSymbol.classId.constructClassLikeType(),
                    )
                }

                return listOf(injectFunction.symbol)
            }

            Names.CreateMethod -> {
                val ownerClassId = owner.classId
                val outerClassId = ownerClassId.outerClassId
                if (ownerClassId.shortClassName != Names.FactoryClass || outerClassId == null) {
                    return emptyList()
                }

                val originalSymbol = symbols.getValue(Unit)[outerClassId] ?: return emptyList()
                val createFunction = createMemberFunction(
                    owner = owner,
                    key = Key,
                    name = callableId.callableName,
                    returnType = outerClassId.defaultType(emptyList())
                ) {
                    modality = Modality.ABSTRACT
                    valueParameter(
                        name = Names.TargetParam,
                        type = originalSymbol.classId.constructClassLikeType(),
                    )
                }

                // It seems like this is the only way to add an annotation to a function param in FIR,
                // there is no API to add it during createMemberFunction.
                // An alternative way is to add the annotation in IR, but not sure metro will be able to read it.
                val targetParam = createFunction.valueParameters.first()
                val targetParamWithProvides = buildValueParameterCopy(targetParam) {
                    symbol = targetParam.symbol
                    annotations += ClassIds.Provides.firAnnotation()
                }
                createFunction.replaceValueParameters(listOf(targetParamWithProvides))

                return listOf(createFunction.symbol)
            }

            else -> error("Unexpected callable name: ${callableId.callableName.asString()}")
        }
    }

    object Key : GeneratedDeclarationKey()
}