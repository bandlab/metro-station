// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.checker

import com.bandlab.metro.station.graph.MetroStationIds as Ids
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId

/**
 * This checker forbids @MetroStation and @StationEntry on Activities that are not in [baseline].
 */
internal class ActivityBaselineChecker(private val baseline: Set<String>) :
    FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val symbol = declaration.symbol
        val session = context.session

        val stationAnnotation =
            symbol.getAnnotationByClassId(Ids.metroStation, session)
                ?: symbol.getAnnotationByClassId(Ids.stationEntry, session)
                ?: return

        val classFqName = declaration.symbol.classId.asSingleFqName().asString()
        if (classFqName !in baseline) {
            reporter.reportOn(
                source = stationAnnotation.source,
                factory = MetroStationDiagnostics.FORBIDDEN_ACTIVITY_USAGE,
                context = context,
            )
        }
    }
}
