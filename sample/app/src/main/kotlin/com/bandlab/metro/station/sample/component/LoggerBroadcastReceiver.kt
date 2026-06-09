package com.bandlab.metro.station.sample.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.utils.Logger
import dev.zacsweers.metro.Inject

@MetroStation(appDependencies = LoggerBroadcastReceiver.ServiceProvider::class)
class LoggerBroadcastReceiver : BroadcastReceiver() {

    @Inject
    private lateinit var logger: Logger

    override fun onReceive(context: Context, intent: Intent?) {
        logger.log("MetroStation:: LoggerBroadcastReceiver received an intent")
    }

    interface ServiceProvider {
        val logger: Logger
    }
}