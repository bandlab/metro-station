package com.bandlab.common.android.pager.screen.di

import com.bandlab.common.android.di.GraphFactory
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

/**
 * Similar with [GraphFactory], but with extra dependencies.
 * This is used in [com.bandlab.uikit.api.page.Page] to ease the graph creation code.
 */
interface PageGraphFactory<Feature, ServiceProvider, ExtraDependencies, Graph> {
    fun create(
        @Provides feature: Feature,
        @Provides pageGraphDependencies: PageGraphDependencies,
        @Includes navPageDependencies: NavPageDependencies = NavPageDependencies(),
        @Includes serviceProvider: ServiceProvider,
        @Includes extraDependencies: ExtraDependencies,
    ): Graph
}