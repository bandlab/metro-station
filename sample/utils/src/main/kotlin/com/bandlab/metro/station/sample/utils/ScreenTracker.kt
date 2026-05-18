package com.bandlab.metro.station.sample.utils

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

interface ScreenTracker {
    fun trackScreenEnter(name: String)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ScreenTrackerImpl(private val logger: Logger) : ScreenTracker {
    override fun trackScreenEnter(name: String) {
        logger.log("Screen entered: $name")
    }
}