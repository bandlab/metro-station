package com.bandlab.metro.station.checker

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

internal class MetroStationFirCheckers(
    session: FirSession,
    private val includeBaselineChecker: Boolean,
    private val stationEntriesBaseline: Set<String>,
) : FirAdditionalCheckersExtension(session) {

    override val declarationCheckers = object : DeclarationCheckers() {
        override val classCheckers = buildSet {
            add(ParamTypeClassChecker)
            add(TargetVisibilityChecker)
            add(MetroStationChecker)

            if (includeBaselineChecker) {
                add(StationEntryBaselineChecker(stationEntriesBaseline))
            }
        }
    }
}