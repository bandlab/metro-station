// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen.di

import androidx.lifecycle.LifecycleOwner
import com.bandlab.android.common.activity.CommonActivity
import com.bandlab.uikit.api.page.PageGraphDependencies

/**
 * A set of dependencies that are required to create a page component. These dependencies will be
 * available in the page graph once the graph is created.
 */
class AndroidPageGraphDependencies(
    val activity: CommonActivity<*>,
    val lifecycleOwner: LifecycleOwner,
) : PageGraphDependencies
