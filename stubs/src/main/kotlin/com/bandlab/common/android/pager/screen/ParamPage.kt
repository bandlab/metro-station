package com.bandlab.common.android.pager.screen

import com.bandlab.common.android.pager.screen.di.PageGraphDependencies
import com.bandlab.common.di.GeneratedByMetroStation
import com.bandlab.uikit.api.page.Page
import kotlinx.coroutines.flow.Flow

/**
 * Use [ParamPage] if you need params for the page, it also supports param updates similar to Activity's onNewIntent.
 * You can inject a [Flow] of [Param] in your VM, whenever the host onNewIntent is triggered, we will invoke
 * [parseParam] and emit the result to the flow, it will be provided by code generator by default.
 *
 * Please note that the [Flow] will include the initial param as well as its first value.
 *
 * If you need only the initial param, inject [Param] directly.
 */
interface ParamPage<ViewModel : Any, Param : Any> : Page<ViewModel> {

    /**
     * Override it if you need to listen to the update from the host's onNewIntent.
     * @param savedState the bundle from the intent, specifically with [OBJECT_ARG] key.
     */
    fun parseParam(savedState: SavedState): Param? = null

    @GeneratedByMetroStation
    fun injectViewModel(deps: PageGraphDependencies, initialParam: Param): ViewModel {
        throw UnsupportedOperationException("This method will be implemented by the compiler plugin.")
    }
}

// Stub for androidx.savedstate.SavedState
object SavedState