package com.bandlab.metro.station.sample.component

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.utils.Logger
import dev.zacsweers.metro.Inject

@MetroStation(appDependencies = LoggerService.ServiceProvider::class)
class LoggerService : Service() {

    @Inject
    private lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()
        logger.log("MetroStation:: LoggerService is bound")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    interface ServiceProvider {
        val logger: Logger
    }
}