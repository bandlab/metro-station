package com.bandlab.metro.station

import com.bandlab.metro.station.fir.MetroStationFirCheckers
import com.bandlab.metro.station.fir.StationEntryGenerator
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class MetroStationPluginRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::MetroStationFirCheckers
        +::StationEntryGenerator
    }
}
