package com.bandlab.metro.station.sample.profile

import android.content.Context
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

@ContributesTo(ProfileActivity::class)
interface ProfileActivityModule {
    @Binds
    val ProfileActivity.bind: Context

    @Provides
    fun provideCoroutineScope(activity: ProfileActivity): CoroutineScope = activity.lifecycleScope
}