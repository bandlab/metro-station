package com.bandlab.metro.station

import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object MetroStationConfigurationKeys {

    const val OPTION_STATION_ENTRIES_BASELINE = "stationEntriesBaseline"

    /**
     * A baseline of features that are allowed to use @StationEntry.
     */
    val STATION_ENTRIES_BASELINE: CompilerConfigurationKey<Set<String>> =
        CompilerConfigurationKey.create(OPTION_STATION_ENTRIES_BASELINE)
}
