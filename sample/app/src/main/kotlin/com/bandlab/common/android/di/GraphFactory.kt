package com.bandlab.common.android.di

import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

/**
 * Base factory interface for building standalone dependency graphs, we use it to ease
 * the component creation code, code gen will first generate factories by extending this
 * type, and we can leverage internal util functions to require factory with types.
 */
interface GraphFactory<Feature, ServiceProvider, ExtraDependencies, Graph> {
    fun create(
        @Provides feature: Feature,
        @Includes serviceProvider: ServiceProvider,
        @Includes extraDependencies: ExtraDependencies,
    ): Graph
}

object EmptyExtraDependencies
