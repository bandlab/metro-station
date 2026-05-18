package com.bandlab.metro.station.sample.utils

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

interface AnalyticsTracker {
    fun trackEvent(event: String)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AnalyticsTrackerImpl(private val logger: Logger) : AnalyticsTracker {
    override fun trackEvent(event: String) {
        logger.log("Tracking event: $event")
    }
}