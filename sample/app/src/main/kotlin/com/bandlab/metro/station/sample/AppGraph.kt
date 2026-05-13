package com.bandlab.metro.station.sample

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Binds
    val SampleApplication.context: Context

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides app: SampleApplication): AppGraph
    }
}