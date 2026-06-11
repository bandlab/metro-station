package com.bandlab.metro.station

import com.bandlab.metro.station.checker.MetroStationFirCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

public class MetroStationPluginRegistrar(
    private val includeBaselineChecker: Boolean,
    private val stationEntriesBaseline: Set<String>,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirAdditionalCheckersExtension.Factory { session ->
            MetroStationFirCheckers(session, includeBaselineChecker, stationEntriesBaseline)
        }
    }
}
