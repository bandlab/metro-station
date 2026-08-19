// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.pager.screen.di

/** The base interface for all page graphs and graph extensions. */
interface PageInjector<ViewModel : Any> {
    fun getPageViewModel(): ViewModel
}
