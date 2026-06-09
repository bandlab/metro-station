package com.bandlab.metro.station.sample.component

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bandlab.metro.station.MetroStation
import com.bandlab.metro.station.sample.utils.Logger
import dev.zacsweers.metro.Inject

@MetroStation(appDependencies = LoggerWorker.ServiceProvider::class)
class LoggerWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @Inject
    private lateinit var logger: Logger

    override suspend fun doWork(): Result {
        logger.log("MetroStation:: LoggerWorker is working")
        return Result.success()
    }

    interface ServiceProvider {
        val logger: Logger
    }
}