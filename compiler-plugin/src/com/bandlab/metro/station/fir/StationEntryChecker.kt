package com.bandlab.metro.station.fir

import com.bandlab.metro.station.MetroStationDiagnostics.STATION_ENTRY_NOT_ON_CLASS
import com.bandlab.metro.station.Symbols
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.modality

/**
 * This FIR checker ensures `@StationEntry` is applied only on final classes.
 */
internal object StationEntryChecker : FirClassChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        if (!declaration.hasAnnotation(Symbols.ClassIds.stationEntry, context.session)) return

        if (declaration.classKind != ClassKind.CLASS || declaration.modality != Modality.FINAL) {
            reporter.reportOn(
                source = declaration.source,
                factory = STATION_ENTRY_NOT_ON_CLASS,
            )
        }
    }
}