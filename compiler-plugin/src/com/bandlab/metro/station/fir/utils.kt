package com.bandlab.metro.station.fir

import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.buildUnaryArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildGetClassCall
import org.jetbrains.kotlin.fir.expressions.builder.buildResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.impl.FirEmptyAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds

internal fun ClassId.firAnnotation(): FirAnnotation = buildAnnotation {
    annotationTypeRef = firTypeRef()
    argumentMapping = FirEmptyAnnotationArgumentMapping
}

internal fun ClassId.firTypeRef(): FirTypeRef = buildResolvedTypeRef {
    coneType = constructClassLikeType()
}

/**
 * Returns a `ClassSymbol::class` expression.
 */
internal fun FirRegularClassSymbol.getClassCall(): FirExpression = buildGetClassCall {
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