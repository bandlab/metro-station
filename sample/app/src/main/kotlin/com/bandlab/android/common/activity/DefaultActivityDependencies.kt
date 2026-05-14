package com.bandlab.android.common.activity

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

/**
 *  Default dependencies for all the activities.
 */
@BindingContainer
interface DefaultActivityDependencies {

    @Binds
    val CommonActivity<*>.asComponentActivity: ComponentActivity

    @Binds
    val ComponentActivity.asLifecycleOwner: LifecycleOwner

    companion object {

        @Provides
        fun provideCoroutineScope(lifecycleOwner: LifecycleOwner): CoroutineScope {
            return lifecycleOwner.lifecycleScope
        }

        @Provides
        fun provideLifecycle(lifecycleOwner: LifecycleOwner): Lifecycle {
            return lifecycleOwner.lifecycle
        }
    }
}