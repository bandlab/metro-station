package com.bandlab.metro.station.sample

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bandlab.common.android.di.HasDependencyGraph
import com.bandlab.metro.station.sample.component.LoggerBroadcastReceiver
import com.bandlab.metro.station.sample.component.LoggerService
import com.bandlab.metro.station.sample.component.LoggerWithoutOnCreateService
import com.bandlab.metro.station.sample.component.LoggerWorker
import com.bandlab.metro.station.sample.main.MainActivity
import com.bandlab.metro.station.sample.profile.ProfileActivity
import com.bandlab.metro.station.sample.profile.content.ProfileContentPage
import com.bandlab.metro.station.sample.profile.footer.ProfileFooterPage
import dev.zacsweers.metro.asContribution
import dev.zacsweers.metro.createGraphFactory

class SampleApp : Application(), HasDependencyGraph {

    private val appGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override fun <T> resolve(): T = HasDependencyGraph.resolveFrom(appGraph)

    override fun onCreate() {
        super.onCreate()

        // Invoke asContribution here to make sure the contributions are processed correctly, and most importantly,
        // to make it a compiler-time error instead of runtime if something goes wrong.
        appGraph.asContribution<MainActivity.FeatureServiceProvider>()
        appGraph.asContribution<ProfileActivity.FeatureExtension.Factory>()
        appGraph.asContribution<ProfileContentPage.FeatureServiceProvider>()
        appGraph.asContribution<ProfileFooterPage.FeatureServiceProvider>()

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