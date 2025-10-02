package com.bandlab.metro.station

import android.app.Activity
import android.content.Context

public fun <T> Context.findEntryFactory(): T {
    val app = applicationContext
    return when {
        this is Activity && this is HasStationEntries -> this.findEntryFactory()
        app is HasStationEntries -> app.findEntryFactory()
        else -> error("Application doesn't implement HasStationEntries interface")
    }
}