package com.bandlab.metro.station.sample.main

import android.content.Context
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

//TODO: This will be generated in FIR
@DependencyGraph(MainActivity::class)
interface MainActivityGraph {

    fun inject(activity: MainActivity)

    @Binds
    val MainActivity.bind: Context

    @Provides
    fun provideCoroutineScope(activity: MainActivity): CoroutineScope = activity.lifecycleScope

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides activity: MainActivity): MainActivityGraph
    }
}