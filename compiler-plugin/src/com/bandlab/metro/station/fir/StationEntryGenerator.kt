package com.bandlab.metro.station.fir

import com.bandlab.metro.station.Predicates
import com.bandlab.metro.station.Symbols
import com.bandlab.metro.station.mapToSet
import com.bandlab.metro.station.plus
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

/**
 * Generates a Metro GraphExtension interface for each class annotated with @StationEntry:
 *
 * ```kotlin
 * @GraphExtension(scope = {Name}::class)
 * interface {Name}GraphExtension {
 *   fun inject(target: {Name})
 *
 *   @ContributesTo(AppScope::class)
 *   @GraphExtension.Factory
 *   interface Factory {
 *     fun create(@Provides target: {Name}): {Name}GraphExtension
 *   }
 * }
 * ```
 */
class StationEntryGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(Predicates.stationEntry)
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun getTopLevelClassIds(): Set<ClassId> {
        return session
            .predicateBasedProvider
            .getSymbolsByPredicate(Predicates.stationEntry)
            // TODO: FIR checker to report non-regular class usages
            .filterIsInstance<FirRegularClassSymbol>()
            .mapToSet {
                ClassId(
                    packageFqName = it.packageFqName(),
                    topLevelName = it.name + Symbols.Names.GraphExtensionClass
                )
            }
    }

    @ExperimentalTopLevelDeclarationsGenerationApi
    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*> {
        val graphExtension = createTopLevelClass(classId, Key, classKind = ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }

        val graphExtensionAnnotation = buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                coneType = Symbols.ClassIds.graphExtension.constructClassLikeType()
            }
            argumentMapping = buildAnnotationArgumentMapping {
                //TODO: params
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
        if (!classSymbol.hasAnnotation(Symbols.ClassIds.graphExtension, session)) {
            return emptySet()
        }
        return setOf(Symbols.Names.FactoryClass)
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*> {
        val factory = createNestedClass(owner, name, Key, classKind = ClassKind.INTERFACE) {
            modality = Modality.ABSTRACT
        }

        val contributesToAnnotation = buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                coneType = Symbols.ClassIds.contributesTo.constructClassLikeType()
            }
            argumentMapping = buildAnnotationArgumentMapping {
            }
        }

        val factoryAnnotation = buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                coneType = Symbols.ClassIds.graphExtensionFactory.constructClassLikeType()
            }
            argumentMapping = buildAnnotationArgumentMapping {
                //TODO: params
            }
        }
        factory.replaceAnnotations(
            listOf(contributesToAnnotation, factoryAnnotation)
        )

        return factory.symbol
    }

//    override fun generateFunctions(
//        callableId: CallableId,
//        context: MemberGenerationContext?
//    ): List<FirNamedFunctionSymbol> {
//        // fun inject(target: {Name})
//        run {
//            val fn = createMemberFunction(top, Key, INJECT_NAME, returnType = session.builtinTypes.unitType.coneType)
//            // parameter type should be original target, but we only know generated classId here.
//            // For a working baseline, use same simple name as the target. Real impl should resolve original target type.
//            // add parameter "target"
//            fn.addValueParameter(Name.identifier("target"), top.classId.shortClassName)
//        }
//        return emptyList()
//    }

//    override fun getCallableNamesForClass(
//        classSymbol: FirClassSymbol<*>,
//        context: MemberGenerationContext
//    ): Set<Name> {
//        return when (classSymbol.classId.shortClassName.asString().endsWith("GraphExtension")) {
//            true -> setOf(INJECT_NAME)
//            false -> if (classSymbol.classId.shortClassName == FACTORY_NAME) setOf(CREATE_NAME) else emptySet()
//        }
//    }

    object Key : GeneratedDeclarationKey()
}