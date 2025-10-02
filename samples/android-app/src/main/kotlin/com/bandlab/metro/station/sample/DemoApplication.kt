package com.bandlab.metro.station.sample

import android.app.Application
import com.bandlab.metro.station.HasStationEntries
import dev.zacsweers.metro.createGraph

class DemoApplication : Application(), HasStationEntries {

    private val appGraph by lazy {
        createGraph<AppGraph>()
    }

    override fun <T> findEntryFactory(): T {
        return HasStationEntries.findFrom(appGraph)
    }
}