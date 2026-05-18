package com.bandlab.common.android.pager.screen.di

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope

/**
 * Provides default dependencies for a Page.
 */
@BindingContainer
object DefaultPageDependencies {

    @Provides
    fun provideLifecycle(lifecycleOwner: LifecycleOwner): Lifecycle {
        return lifecycleOwner.lifecycle
    }

    @Provides
    fun provideLifecycleScope(lifecycleOwner: LifecycleOwner): CoroutineScope {
        return lifecycleOwner.lifecycle.coroutineScope
    }
}