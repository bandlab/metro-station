package com.bandlab.metro.station.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

internal class MetroStationFirCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers: DeclarationCheckers =
        object : DeclarationCheckers() {
            override val classCheckers: Set<FirClassChecker>
                get() = setOf(
                    MetroStationChecker,
                    StationEntryChecker
                )
        }
}