// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen

import androidx.savedstate.SavedState
import com.bandlab.common.di.GeneratedByMetroStation
import com.bandlab.uikit.api.page.Page
import com.bandlab.uikit.api.page.PageGraphDependencies
import kotlinx.coroutines.flow.Flow

/**
 * Use [ParamPage] if you need params for the page. The compiler plugin provides both [Param] and a
 * [Flow] of [Param] for injection into your ViewModel.
 *
 * The sample's [PageParamFlowProvider] emits only the initial param. The internal framework also
 * calls [parseParam] on host activity updates and emits the result; that wiring is omitted here.
 *
 * If you need only the initial param, inject [Param] directly.
 */
interface ParamPage<ViewModel : Any, Param : Any> : Page<ViewModel> {

    /**
     * Hook used by the internal framework for host onNewIntent updates; not called in this sample.
     *
     * @param savedState the bundle from the intent.
     */
    fun parseParam(savedState: SavedState): Param? = null

    /**
     * Creates the dependency graph (or graph extension), and returns the ViewModel instance for the
     * page.
     */
    @GeneratedByMetroStation
    fun injectViewModel(deps: PageGraphDependencies, initialParam: Param): ViewModel {
        throw UnsupportedOperationException(
            "This method will be implemented by the compiler plugin.",
        )
    }
}
