package com.bandlab.common.android.pager.screen.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides

@BindingContainer
class NavPageDependencies(
    private val navPageNavigation: NavPageNavigation = NavPageNavigation.NOOP,
) {
    @Provides
    fun provideNavPageNavigation(): NavPageNavigation = navPageNavigation
}