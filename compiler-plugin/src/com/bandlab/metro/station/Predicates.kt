package com.bandlab.metro.station

import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated

object Predicates {
    val metroStation = annotated(Symbols.ClassIds.metroStation.asSingleFqName())
    val stationEntry = annotated(Symbols.ClassIds.stationEntry.asSingleFqName())
}