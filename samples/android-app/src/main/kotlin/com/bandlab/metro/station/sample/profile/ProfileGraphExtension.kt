package com.bandlab.metro.station.sample.profile

import android.content.Context
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.*
import kotlinx.coroutines.CoroutineScope

@GraphExtension(scope = ProfileActivity::class)
interface ProfileGraphExtension {

    fun inject(activity: ProfileActivity)

    @Binds
    val ProfileActivity.bind: Context

    @Provides
    fun provideCoroutineScope(activity: ProfileActivity): CoroutineScope = activity.lifecycleScope

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides activity: ProfileActivity): ProfileGraphExtension
    }
}