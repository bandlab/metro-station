package com.bandlab.common.android.pager.screen.di

import androidx.lifecycle.LifecycleOwner
import com.bandlab.android.common.activity.CommonActivity
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides

@BindingContainer
object PageGraphDependenciesModule {

    @Provides
    fun provideCommonActivity(graphDeps: PageGraphDependencies): CommonActivity<*> = graphDeps.activity

    @Provides
    fun provideLifecycleOwner(graphDeps: PageGraphDependencies): LifecycleOwner = graphDeps.lifecycleOwner
}