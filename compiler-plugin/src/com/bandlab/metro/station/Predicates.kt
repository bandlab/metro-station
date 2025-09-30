package com.bandlab.metro.station

import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate.BuilderContext.annotated

internal object Predicates {
    val metroStation = annotated(Symbols.ClassIds.MetroStation.asSingleFqName())
    val stationEntry = annotated(Symbols.ClassIds.StationEntry.asSingleFqName())
}