package com.bandlab.metro.station.utils

import dev.zacsweers.metro.compiler.fir.MetroFirTypeResolver
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.getSingleClassifier
import org.jetbrains.kotlin.fir.scopes.impl.declaredMemberScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.*

internal fun String.asName(): Name = Name.identifier(this)

internal infix operator fun Name.plus(other: String) = (asString() + other).asName()
internal infix operator fun Name.plus(other: Name) = (asString() + other.asString()).asName()

internal fun ClassId.toCallableId() = CallableId(packageFqName, shortClassName)

/**
 * Builds a simple FIR annotation with the specified class ID and argument mapping.
 *
 * @param classId The class ID representing the annotation's type.
 * @param argumentMapping The mapping of arguments for the annotation. Defaults to an empty mapping if not provided.
 * @return A [FirAnnotation] instance with the specified type and argument mapping.
 */
internal fun buildSimpleAnnotation(
    classId: ClassId,
    argumentMapping: FirAnnotationArgumentMapping = buildAnnotationArgumentMapping()
): FirAnnotation {
    return buildAnnotation {
        annotationTypeRef =
            ConeClassLikeTypeImpl(
                ConeClassLikeLookupTagImpl(classId),
                emptyArray(),
                isMarkedNullable = false,
            ).toFirResolvedTypeRef()
        this.argumentMapping = argumentMapping
    }
}

/**
 * Builds a simple FIR annotation call with the provided parameters.
 *
 * @param session The FIR session used to resolve symbols and types.
 * @param classId The class ID of the annotation being constructed.
 * @param containingSymbol The symbol representing the containing declaration of the annotation.
 * @param argumentMapping The mapping of arguments for the annotation. Defaults to an empty mapping.
 * @return A resolved FIR annotation call based on the provided information.
 */
@OptIn(DirectDeclarationsAccess::class)
internal fun buildSimpleAnnotationCall(
    session: FirSession,
    classId: ClassId,
    containingSymbol: FirBasedSymbol<*>,
    argumentMapping: FirAnnotationArgumentMapping = buildAnnotationArgumentMapping()
): FirAnnotationCall {
    val annotationType = ConeClassLikeTypeImpl(
        ConeClassLikeLookupTagImpl(classId),
        emptyArray(),
        isMarkedNullable = false,
    )
    return buildAnnotationCall {
        annotationTypeRef = annotationType.toFirResolvedTypeRef()
        this.argumentMapping = argumentMapping
        calleeReference = buildResolvedNamedReference {
            name = classId.shortClassName
            resolvedSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)!!.let {
                (it as FirClassSymbol<*>)
                    .declarationSymbols
                    .filterIsInstance<FirConstructorSymbol>()
                    .first()
            }
        }
        containingDeclarationSymbol = containingSymbol
        annotationResolvePhase = FirAnnotationResolvePhase.Types
    }
}

/**
 * Returns a `ClassSymbol::class` expression.
 */
internal fun FirClassLikeSymbol<*>.getClassCall(): FirExpression = buildGetClassCall {
    argumentList = buildUnaryArgumentList(
        buildResolvedQualifier {
            packageFqName = classId.packageFqName
            relativeClassFqName = classId.relativeClassName
            symbol = this@getClassCall
            resolvedToCompanionObject = false
            coneTypeOrNull = this@getClassCall.defaultType()
        }
    )
    coneTypeOrNull = StandardClassIds.KClass.constructClassLikeType(
        arrayOf(this@getClassCall.defaultType())
    )
}

/**
 * Marks a function as abstract.
 */
internal fun FirFunction.markAbstract(owner: FirClassSymbol<*>) {
    replaceStatus(
        FirResolvedDeclarationStatusImpl(
            Visibilities.Public,
            Modality.ABSTRACT,
            Visibilities.Public.toEffectiveVisibility(owner, forClass = true),
        )
    )
}

/**
 * Finds the supertype reference of the class represented by this [FirClassSymbol] that matches
 * the given [supertypeClassId].
 *
 * The method iterates through all supertypes of the class and checks if any has a corresponding
 * [ClassId] matching the specified [supertypeClassId].
 *
 * @param supertypeClassId the [ClassId] of the desired supertype to search for.
 * @return the [FirTypeRef] of the matching supertype if found, or `null` otherwise.
 */
@OptIn(SymbolInternals::class)
internal fun FirClassSymbol<*>.findSuperTypeRef(supertypeClassId: ClassId): FirTypeRef? {
    for (ref in fir.superTypeRefs) {
        when (ref) {
            is FirUserTypeRef if (ref.qualifier.lastOrNull()?.name == supertypeClassId.shortClassName) -> return ref
            is FirResolvedTypeRef if (ref.coneType.classId == supertypeClassId) -> return ref
        }
    }
    return null
}

/**
 * Resolves the specified supertype of a class symbol by deeply traversing its hierarchy,
 * including supertypes of the current class and their respective supertypes.
 *
 * @param supertypeClassId The [ClassId] of the supertype to resolve.
 * @param session The [FirSession] associated with the resolution process,
 *                providing necessary context and symbol resolution capabilities.
 * @return A [FirTypeRef] representing the resolved supertype if found, or `null` if the
 *         specified supertype could not be resolved.
 */
@OptIn(SymbolInternals::class)
context(typeResolver: MetroFirTypeResolver)
internal fun FirClassSymbol<*>.deepResolveSuperType(supertypeClassId: ClassId, session: FirSession): FirTypeRef? {
    val visited = mutableSetOf<ClassId>()
    val queue = ArrayDeque<FirClassSymbol<*>>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        val currentId = current.classId
        if (!visited.add(currentId)) continue
        if (currentId == supertypeClassId) return current.defaultType().toFirResolvedTypeRef()

        for (typeRef in current.fir.superTypeRefs) {
            when (typeRef) {
                is FirUserTypeRef if (typeRef.qualifier.lastOrNull()?.name == supertypeClassId.shortClassName) -> return typeRef
                is FirResolvedTypeRef if (typeRef.coneType.classId == supertypeClassId) -> return typeRef
            }

            val resolvedType = try {
                typeResolver.resolveType(typeRef)
            } catch (_: IllegalArgumentException) {
                continue
            }
            val classId = resolvedType.classId ?: continue
            val symbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)
            if (classId == supertypeClassId) return resolvedType.toFirResolvedTypeRef()
            if (symbol is FirClassSymbol<*>) {
                queue.add(symbol)
            }
        }
    }
    return null
}

/**
 * Unwraps the type referenced by the given [FirTypeRef] based on its structure and optional index.
 *
 * @param index An optional index used to retrieve a specific type argument from the type reference. Defaults to 0.
 * @return A [ConeTypeProjection] representing the unwrapped type if applicable, or `null` if the type could not be unwrapped.
 */
internal fun FirTypeRef.unwrapType(index: Int = 0): ConeTypeProjection? {
    return when (this) {
        is FirUserTypeRef -> {
            val typeArg = qualifier.lastOrNull()?.typeArgumentList?.typeArguments?.getOrNull(index)
                as? FirTypeProjectionWithVariance
            val typeRef = typeArg?.typeRef as? FirResolvedTypeRef
            typeRef?.coneType
        }

        is FirResolvedTypeRef -> {
            val coneType = coneType as? ConeClassLikeType
            coneType?.typeArguments?.getOrNull(index)
        }

        else -> null
    }
}

/**
 * Returns the [KtSourceElement] of the type argument at the given [index] from this [FirTypeRef].
 */
internal fun FirTypeRef.typeArgumentSource(index: Int = 0): KtSourceElement? {
    return when (this) {
        is FirUserTypeRef -> {
            val typeArg = qualifier.lastOrNull()?.typeArgumentList?.typeArguments?.getOrNull(index)
            typeArg?.source
        }

        is FirResolvedTypeRef -> {
            delegatedTypeRef?.typeArgumentSource(index)
        }

        else -> null
    }
}

/**
 * Recursively extract a [ClassId] from an unresolved property access expression chain.
 */
internal fun FirExpression.extractClassId(ownerClassId: ClassId, session: FirSession): ClassId? {
    val names = mutableListOf<String>()
    var current: FirExpression? = this
    while (current is FirPropertyAccessExpression) {
        val ref = current.calleeReference
        names.add(0, ref.name.asString())
        current = current.explicitReceiver
    }
    if (names.isEmpty()) return null

    val packageFqName = ownerClassId.packageFqName
    for (i in names.indices) {
        val outerName = names.subList(0, i + 1).joinToString(".")
        val classId = ClassId(packageFqName, FqName(outerName), false)
        if (session.symbolProvider.getClassLikeSymbolByClassId(classId) != null) {
            var result = classId
            for (j in (i + 1) until names.size) {
                result = result.createNestedClassId(names[j].asName())
            }
            if (session.symbolProvider.getClassLikeSymbolByClassId(result) != null) {
                return result
            }
        }
    }
    return null
}

/**
 * Access the declared member scope to trigger Metro's FIR generator, which creates the
 * MetroContribution nested class inside our MultibindingContribution. Without this, Metro
 * wouldn't see our contribution.
 */
internal fun ClassId.findMetroContributionSymbol(session: FirSession): FirRegularClassSymbol? {
    val contributionSymbol = session.symbolProvider.getClassLikeSymbolByClassId(this)
        as? FirRegularClassSymbol ?: return null

    val scope = contributionSymbol.declaredMemberScope(session, memberRequiredPhase = null)
    val metroContributionName = scope.getClassifierNames()
        .firstOrNull { it.identifier.startsWith("MetroContributionTo") }
        ?: return null

    return scope.getSingleClassifier(metroContributionName) as? FirRegularClassSymbol
}