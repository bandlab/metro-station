package com.bandlab.metro.station.sample.main

import android.content.Context
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

@ContributesTo(MainActivity::class)
interface MainActivityModule {
    @Binds
    val MainActivity.bind: Context

    @Provides
    fun provideCoroutineScope(activity: MainActivity): CoroutineScope = activity.lifecycleScope
}