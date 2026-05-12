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
 *  Default dependencies for all the activities, please do not add feature-specific dependencies
 *  here, feature specific dependencies should be contributed explicitly to where we need them.
 *
 *  _If we want to add any new dependency, please discuss with the team before doing this._
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