package com.bandlab.metro.extensions.sample.utils

import com.bandlab.metro.extensions.sample.SampleApplication
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import java.util.logging.Level
import java.util.logging.Logger

interface ScreenTracker {
    fun trackScreenEnter(name: String)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ScreenTrackerImpl(application: SampleApplication) : ScreenTracker {
    override fun trackScreenEnter(name: String) {
        Logger.getLogger("sample").log(Level.ALL, "Entering screen $name")
    }
}