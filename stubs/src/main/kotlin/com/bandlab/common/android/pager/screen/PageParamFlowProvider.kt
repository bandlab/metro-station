// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * JVM test stub that emits only the initial page parameter.
 *
 * This verifies generated parameter-flow bindings without simulating deep links, host activity
 * updates, or saved-state restoration.
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
