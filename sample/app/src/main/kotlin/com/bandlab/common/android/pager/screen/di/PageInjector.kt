package com.bandlab.common.android.pager.screen.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import kotlin.reflect.KClass

/**
 * The base interface for all page graphs and graph extensions.
 */
interface PageInjector<ViewModel : Any> {
    fun getPageViewModel(): ViewModel
}