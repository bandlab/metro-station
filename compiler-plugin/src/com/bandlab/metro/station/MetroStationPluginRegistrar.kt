package com.bandlab.metro.station

import com.bandlab.metro.station.checker.MetroStationFirCheckers
import com.bandlab.metro.station.graph.MetroStationSupertypeGenerator
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension

public class MetroStationPluginRegistrar(
    private val includeBaselineChecker: Boolean,
    private val stationEntriesBaseline: Set<String>,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirAdditionalCheckersExtension.Factory { session ->
            MetroStationFirCheckers(session, includeBaselineChecker, stationEntriesBaseline)
        }
        +FirSupertypeGenerationExtension.Factory(::MetroStationSupertypeGenerator)
    }
}
