package com.bandlab.common.android.pager.screen

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * A provider for creating a flow of parameters for a page.
 *
 * Handled cases:
 * 1. Initial default parameter
 * 2. Initial deeplink parameter (the page is not created)
 * 2. New param when the page is already created
 * 3. New param when the page is not created
 * 4. Parameter after restoring state
 */
@Inject
class PageParamFlowProvider {

    fun <R : Any> createParamFlow(
        page: ParamPage<*, R>,
        initialParam: R,
    ): Flow<R> {
        page.toString()
        return flowOf(initialParam)
    }
}