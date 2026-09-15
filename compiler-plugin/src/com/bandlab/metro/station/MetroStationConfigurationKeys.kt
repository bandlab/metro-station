// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station

import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object MetroStationConfigurationKeys {

    const val OPTION_ALLOW_STATION_ENTRIES = "allowStationEntries"

    const val OPTION_STATION_ENTRIES_BASELINE = "stationEntriesBaseline"

    /**
     * Whether @StationEntry is allowed. When false, the whole station entry pipeline is disabled.
     */
    val ALLOW_STATION_ENTRIES: CompilerConfigurationKey<Boolean> =
        CompilerConfigurationKey.create(OPTION_ALLOW_STATION_ENTRIES)

    /** A baseline of features that are allowed to use @StationEntry. */
    val STATION_ENTRIES_BASELINE: CompilerConfigurationKey<Set<String>> =
        CompilerConfigurationKey.create(OPTION_STATION_ENTRIES_BASELINE)
}
