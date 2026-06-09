package com.bandlab.metro.station.sample

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bandlab.common.android.di.HasServiceProvider
import com.bandlab.metro.station.sample.component.LoggerBroadcastReceiver
import com.bandlab.metro.station.sample.component.LoggerService
import com.bandlab.metro.station.sample.component.LoggerWithoutOnCreateService
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

        // Start LoggerWithoutOnCreateService
        bindService(
            Intent(this, LoggerWithoutOnCreateService::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) = Unit
                override fun onServiceDisconnected(name: ComponentName?) = Unit
            },
            BIND_AUTO_CREATE
        )

        // Trigger LoggerBroadcastReceiver
        sendBroadcast(Intent(this, LoggerBroadcastReceiver::class.java))

        // Enqueue LoggerWorker
        val workRequest = OneTimeWorkRequestBuilder<LoggerWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}