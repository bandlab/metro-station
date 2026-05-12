package com.bandlab.common.android.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.MembersInjector
import dev.zacsweers.metro.Multibinds
import kotlin.reflect.KClass

interface MembersInjectorProvider<T : Any> {
    val injector: MembersInjector<T>
}

/**
 * Multibinding of graph extension factories.
 */
typealias DispatchingGraphExtensionFactories = Map<KClass<*>, Any>

@ContributesTo(AppScope::class)
interface GraphExtensionFactoriesProvider {

    @Multibinds(allowEmpty = true)
    val dispatchingGraphExtensionFactories: DispatchingGraphExtensionFactories
}
