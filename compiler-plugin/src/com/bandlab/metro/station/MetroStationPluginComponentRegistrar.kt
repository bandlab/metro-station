package com.bandlab.metro.station

import com.bandlab.metro.station.entry.StationEntryIr
import com.bandlab.metro.station.graph.MetroStationIr
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

public class MetroStationPluginComponentRegistrar : CompilerPluginRegistrar() {

    private companion object {
        val isIde by lazy {
            try {
                // Try to look up an IntelliJ-only class
                Class.forName("org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
    }

    override val pluginId: String get() = BuildConfig.KOTLIN_PLUGIN_ID
    override val supportsK2: Boolean get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val stationEntriesBaseline = configuration.get(MetroStationConfigurationKeys.STATION_ENTRIES_BASELINE)
        FirExtensionRegistrarAdapter.registerExtension(
            MetroStationPluginRegistrar(
                includeBaselineChecker = stationEntriesBaseline != null,
                stationEntriesBaseline = stationEntriesBaseline.orEmpty()
            )
        )

        // Do not run IR extensions in IDE
        if (!isIde) {
            IrGenerationExtension.registerExtension(MetroStationIr())
            IrGenerationExtension.registerExtension(StationEntryIr())
        }
    }
}
