package com.bandlab.common.android.pager.screen.di

import androidx.lifecycle.LifecycleOwner
import com.bandlab.android.common.activity.CommonActivity

/**
 * A set of dependencies that are required to create a page component.
 */
data class PageGraphDependencies(
    val initialParam: Any = Unit,
    val activity: CommonActivity<*>,
    val lifecycleOwner: LifecycleOwner,
)