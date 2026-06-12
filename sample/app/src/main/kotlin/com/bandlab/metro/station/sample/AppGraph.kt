package com.bandlab.metro.station.sample

import android.content.Context
import com.bandlab.config.api.DebuggableConfigSelector
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AppGraph {

    @Binds
    val SampleApp.context: Context

    // Add an accessor here to make sure config selector is not empty
    val configSelectors: Set<DebuggableConfigSelector>

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides app: SampleApp): AppGraph
    }
}