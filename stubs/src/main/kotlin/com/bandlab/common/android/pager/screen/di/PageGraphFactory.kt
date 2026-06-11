package com.bandlab.common.android.pager.screen.di

import com.bandlab.android.common.activity.CommonActivity
import com.bandlab.common.android.di.GraphExtensionFactory
import com.bandlab.common.android.di.GraphFactory
import com.bandlab.common.android.di.resolveServiceProvider
import com.bandlab.common.android.pager.screen.ParamPage
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
        @Includes pageGraphDependencies: PageGraphDependencies,
        @Includes serviceProvider: ServiceProvider,
        @Includes extraDependencies: ExtraDependencies,
    ): Graph
}

/**
 * Similar with [GraphExtensionFactory], but with page-specific dependencies.
 */
interface PageGraphExtensionFactory<Feature, VM : Any, Param, Graph : PageInjector<VM>> {
    fun create(
        @Provides feature: Feature,
        @Provides param: Param,
        @Includes pageGraphDependencies: PageGraphDependencies,
    ): Graph
}

/**
 * Create the page graph and inject the ViewModel.
 */
fun <ViewModel : Any, Param : Any> Page<ViewModel>.createPageViewModel(
    initialParam: Param,
    host: CommonActivity<*>,
): ViewModel {
    val pageDependencies = PageGraphDependencies(
        activity = host,
    )
    return if (this is ParamPage<*, *>) {
        @Suppress("UNCHECKED_CAST")
        this as ParamPage<ViewModel, Param>
        injectViewModel(pageDependencies, initialParam)
    } else {
        injectViewModel(pageDependencies)
    }
}

/**
 * A helper function to create the graph and inject the ViewModel, it will be called in the generated code of [Page.injectViewModel].
 */
fun <Feature : Page<*>, VM : Any, Param, ServiceProvider, ExtraDependencies, Graph : PageInjector<VM>>
    Feature.createGraphAndInjectViewModel(
    deps: PageGraphDependencies,
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
