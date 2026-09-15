// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.services

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object MetroDirectives : SimpleDirectivesContainer() {
    val DISABLE_STATION_ENTRIES by directive("Disable the whole @StationEntry pipeline.")
}
