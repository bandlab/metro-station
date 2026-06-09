package com.bandlab.metro.station.sample

import android.app.Application
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bandlab.common.android.di.HasServiceProvider
import com.bandlab.metro.station.sample.component.LoggerBroadcastReceiver
import com.bandlab.metro.station.sample.component.LoggerService
import com.bandlab.metro.station.sample.component.LoggerWorker
import dev.zacsweers.metro.createGraphFactory

class SampleApp : Application(), HasServiceProvider {

    private val appGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override fun <T> resolve(): T = HasServiceProvider.resolveFrom(appGraph)

    override fun onCreate() {
        super.onCreate()

        // Start LoggerService
        startService(Intent(this, LoggerService::class.java))

        // Trigger LoggerBroadcastReceiver
        sendBroadcast(Intent(this, LoggerBroadcastReceiver::class.java))

        // Enqueue LoggerWorker
        val workRequest = OneTimeWorkRequestBuilder<LoggerWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}