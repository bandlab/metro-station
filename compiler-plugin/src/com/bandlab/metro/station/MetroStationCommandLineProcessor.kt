package com.bandlab.metro.station

import com.bandlab.metro.station.MetroStationConfigurationKeys.OPTION_STATION_ENTRIES_BASELINE
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

public class MetroStationCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String get() = BuildConfig.KOTLIN_PLUGIN_ID
    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = OPTION_STATION_ENTRIES_BASELINE,
            valueDescription = "A string that represents a set of fully qualified class names, separated by colon",
            description = "Fully qualified class names to use the deprecated StationEntry feature",
            required = false,
        ),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            OPTION_STATION_ENTRIES_BASELINE -> configuration.put(
                MetroStationConfigurationKeys.STATION_ENTRIES_BASELINE,
                value.split(":").toSet()
            )

            else -> error("Unexpected config option: '${option.optionName}'")
        }
    }
}
