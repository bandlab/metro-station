package com.bandlab.metro.station.sample

import android.app.Application
import dev.zacsweers.metro.createGraph

class DemoApplication : Application(), HasStationEntry {

    private val appGraph by lazy {
        createGraph<AppGraph>()
    }

    override fun <T> nowArriving(): T {
        @Suppress("UNCHECKED_CAST")
        return appGraph as T
    }
}