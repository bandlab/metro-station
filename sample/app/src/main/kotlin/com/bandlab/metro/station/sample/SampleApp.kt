package com.bandlab.metro.station.sample

import android.app.Application
import com.bandlab.common.android.di.HasServiceProvider
import dev.zacsweers.metro.createGraphFactory

class SampleApp : Application(), HasServiceProvider {

    private val appGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override fun <T> resolve(): T = HasServiceProvider.resolveFrom(appGraph)
}