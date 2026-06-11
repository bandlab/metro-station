package com.bandlab.common.android.pager.screen.di

import com.bandlab.common.android.di.GraphFactory
import com.bandlab.common.android.di.resolveServiceProvider
import com.bandlab.uikit.api.page.Page
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

/**
 * Similar with [GraphFactory], but with page-specific dependencies.
 */
interface PageGraphFactory<
    Feature,
    VM : Any,
    Param,
    ServiceProvider,
    ExtraDependencies,
    Graph : PageInjector<VM>
    > {
    fun create(
        @Provides feature: Feature,
        @Provides param: Param,
        @Includes pageGraphDependencies: AndroidPageGraphDependencies,
        @Includes serviceProvider: ServiceProvider,
        @Includes extraDependencies: ExtraDependencies,
    ): Graph
}

/**
 * A helper function to create the graph and inject the ViewModel, it will be called in the generated code of [Page.injectViewModel].
 */
fun <Feature : Page<*>, VM : Any, Param, ServiceProvider, ExtraDependencies, Graph : PageInjector<VM>>
    Feature.createGraphAndInjectViewModel(
    deps: AndroidPageGraphDependencies,
    param: Param,
    factory: PageGraphFactory<Feature, VM, Param, ServiceProvider, ExtraDependencies, Graph>,
    extraDependencies: ExtraDependencies,
): VM {
    val graph = factory.create(
        feature = this,
        param = param,
        pageGraphDependencies = deps,
        extraDependencies = extraDependencies,
        serviceProvider = deps.activity.resolveServiceProvider()
    )
    return graph.getPageViewModel()
}
