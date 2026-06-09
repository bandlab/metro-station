package com.bandlab.metro.station.sample.component

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.utils.Logger
import dev.zacsweers.metro.Inject

@MetroStation(appDependencies = LoggerWithoutOnCreateService.ServiceProvider::class)
class LoggerWithoutOnCreateService : Service() {

    @Inject
    private lateinit var logger: Logger

    override fun onBind(intent: Intent?): IBinder? {
        logger.log("MetroStation:: LoggerWithoutOnCreateService is bound")
        return null
    }

    interface ServiceProvider {
        val logger: Logger
    }
}