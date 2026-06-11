package com.bandlab.common.android.pager.screen.di

import com.bandlab.android.common.activity.CommonActivity

/**
 * A set of dependencies that are required to create a page component.
 */
data class PageGraphDependencies(
    val activity: CommonActivity<*>,
    val navPageNavigation: NavPageNavigation = NavPageNavigation.NOOP,
)