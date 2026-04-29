package com.bandlab.metro.extensions.utils

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

internal fun String.asName(): Name = Name.identifier(this)

internal infix operator fun Name.plus(other: String) = (asString() + other).asName()
internal infix operator fun Name.plus(other: Name) = (asString() + other.asString()).asName()

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
internal fun FirNamedFunction.markAbstract(owner: FirClassSymbol<*>) {
    replaceStatus(
        FirResolvedDeclarationStatusImpl(
            Visibilities.Public,
            Modality.ABSTRACT,
            Visibilities.Public.toEffectiveVisibility(owner, forClass = true),
        )
    )
}