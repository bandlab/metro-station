package com.bandlab.metro.station.fir

import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.impl.FirEmptyAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.ClassId

internal fun ClassId.firAnnotation(): FirAnnotation = buildAnnotation {
    annotationTypeRef = firTypeRef()
    argumentMapping = FirEmptyAnnotationArgumentMapping
}

internal fun ClassId.firTypeRef(): FirTypeRef = buildResolvedTypeRef {
    coneType = constructClassLikeType()
}