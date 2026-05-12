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

typealias DispatchingPageInjector = Map<KClass<*>, Any>

@ContributesTo(AppScope::class)
interface PageInjectorProvider {

    @Multibinds(allowEmpty = true)
    val dispatchingPageInjector: DispatchingPageInjector
}
