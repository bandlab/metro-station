package com.bandlab.metro.station.sample.profile

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
)
