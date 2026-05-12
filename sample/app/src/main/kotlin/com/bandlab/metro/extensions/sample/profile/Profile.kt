package com.bandlab.metro.extensions.sample.profile

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
)
