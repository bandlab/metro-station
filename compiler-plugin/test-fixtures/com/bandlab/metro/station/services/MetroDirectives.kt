// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.services

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object MetroDirectives : SimpleDirectivesContainer() {
    val ENABLE_STATION_ENTRIES_BASELINE by directive("Enable the StationEntry baseline checker.")
    val GENERATE_CLASSES_IN_IR by directive("Generate the Classes in ir.")
}
