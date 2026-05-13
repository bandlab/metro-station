package com.bandlab.metro.station.checker

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import com.bandlab.metro.station.graph.MetroStationIds as Ids

internal class StationEntryBaselineChecker(
    private val baseline: Set<String>,
) : FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val symbol = declaration.symbol
        val session = context.session

        // Only check classes annotated @StationEntry
        val stationEntryAnnotation = symbol.getAnnotationByClassId(Ids.stationEntry, session) ?: return

        val classFqName = declaration.symbol.classId.asSingleFqName().asString()
        if (classFqName !in baseline) {
            reporter.reportOn(
                source = stationEntryAnnotation.source,
                factory = MetroStationDiagnostics.DEPRECATED_CONTRIBUTES_INJECTOR,
                context = context
            )
        }
    }
}