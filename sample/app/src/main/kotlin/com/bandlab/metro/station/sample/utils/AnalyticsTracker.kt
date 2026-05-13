package com.bandlab.metro.station.sample.utils

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import java.util.logging.Level
import java.util.logging.Logger

interface AnalyticsTracker {
    fun trackEvent(event: String)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AnalyticsTrackerImpl : AnalyticsTracker {
    override fun trackEvent(event: String) {
        Logger.getLogger("sample").log(Level.ALL, "Tracking event: $event")
    }
}