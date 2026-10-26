// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen

import com.bandlab.common.di.GeneratedByMetroStation
import com.bandlab.uikit.api.page.Page
import com.bandlab.uikit.api.page.PageGraphDependencies
import kotlinx.coroutines.flow.Flow

/**
 * JVM test contract for a page with typed parameters.
 *
 * The compiler plugin provides [Param] and a [Flow] of [Param] for injection into the ViewModel.
 *
 * The stub [PageParamFlowProvider] emits only the initial param; it does not model host activity
 * updates or saved-state restoration.
 *
 * If you need only the initial param, inject [Param] directly.
 */
interface ParamPage<ViewModel : Any, Param : Any> : Page<ViewModel> {

    /**
     * Hook used by the internal framework for host activity updates; not called by the JVM stub.
     *
     * @param savedState the saved-state stub for the incoming parameters.
     */
    fun parseParam(savedState: SavedState): Param? = null

    @GeneratedByMetroStation
    fun injectViewModel(deps: PageGraphDependencies, initialParam: Param): ViewModel {
        throw UnsupportedOperationException(
            "This method will be implemented by the compiler plugin.",
        )
    }
}

// Stub for androidx.savedstate.SavedState
object SavedState
