package com.bandlab.common.android.di

import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

/**
 * Base factory interface for building standalone dependency graphs.
 */
interface GraphFactory<Feature, ServiceProvider, ExtraDependencies, Graph> {
    fun create(
        @Provides feature: Feature,
        @Includes serviceProvider: ServiceProvider,
        @Includes extraDependencies: ExtraDependencies,
    ): Graph
}

/**
 * Base factory interface for building graph extensions.
 */
interface GraphExtensionFactory<Feature, GraphExtension> {
    fun create(@Provides feature: Feature): GraphExtension
}

object EmptyExtraDependencies