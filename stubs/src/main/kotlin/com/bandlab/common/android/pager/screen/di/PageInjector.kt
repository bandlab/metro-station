// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen.di

/**
 * The base interface for `@StationEntry` page graph extensions. `@MetroStation` graphs expose a
 * generated `viewModel` accessor instead.
 */
interface PageInjector<ViewModel : Any> {
    fun getPageViewModel(): ViewModel
}
