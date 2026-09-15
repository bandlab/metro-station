// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.checker

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

internal class MetroStationFirCheckers(
    session: FirSession,
    private val allowStationEntries: Boolean,
    private val stationEntriesBaseline: Set<String>?,
) : FirAdditionalCheckersExtension(session) {

    override val declarationCheckers =
        object : DeclarationCheckers() {
            override val classCheckers = buildSet {
                add(ParamTypeClassChecker)
                add(TargetVisibilityChecker)
                add(MetroStationChecker)

                if (!allowStationEntries || !stationEntriesBaseline.isNullOrEmpty()) {
                    add(
                        StationEntryBaselineChecker(
                            allowStationEntries,
                            stationEntriesBaseline.orEmpty(),
                        )
                    )
                }
            }
        }
}
