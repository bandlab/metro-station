package com.bandlab.metro.extensions.sample

import android.app.Application
import com.bandlab.common.android.di.HasServiceProvider
import dev.zacsweers.metro.createGraphFactory

class SampleApplication : Application(), HasServiceProvider {

    private val appGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override fun <T> resolve(): T = HasServiceProvider.resolveFrom(appGraph)
}