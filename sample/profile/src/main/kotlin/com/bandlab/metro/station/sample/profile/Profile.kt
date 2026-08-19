// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.sample.profile

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
)
