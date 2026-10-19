// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Simplified sample provider that emits only the initial page parameter.
 *
 * The internal implementation also handles deep links, host activity parameter updates, and
 * saved-state restoration. Those behaviors are omitted from this sample.
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
