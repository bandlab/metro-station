// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen.di

import androidx.lifecycle.LifecycleOwner
import com.bandlab.android.common.activity.CommonActivity
import com.bandlab.common.android.pager.screen.ParamPage
import com.bandlab.common.di.GeneratedByMetroStation
import com.bandlab.uikit.api.page.Page

/**
 * Create the page graph and inject the ViewModel, by calling the generated code of
 * [Page.injectViewModel] or [ParamPage.injectViewModel].
 */
fun <ViewModel : Any, Param : Any> Page<ViewModel>.createPageViewModel(
    initialParam: Param,
    host: CommonActivity<*>,
    lifecycleOwner: LifecycleOwner,
): ViewModel {
    val pageDependencies =
        AndroidPageGraphDependencies(
            activity = host,
            lifecycleOwner = lifecycleOwner,
        )
    @OptIn(GeneratedByMetroStation::class)
    return if (this is ParamPage<*, *>) {
        @Suppress("UNCHECKED_CAST")
        this as ParamPage<ViewModel, Param>
        injectViewModel(pageDependencies, initialParam)
    } else {
        injectViewModel(pageDependencies)
    }
}
