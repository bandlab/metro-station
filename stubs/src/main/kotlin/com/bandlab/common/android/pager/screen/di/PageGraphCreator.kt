package com.bandlab.common.android.pager.screen.di

import android.content.Context
import com.bandlab.uikit.api.page.Page

/**
 * A component creator for [Page] that will initialize in the composable.
 */
class PageGraphCreator<T : Any>(private val initializer: (PageGraphDependencies) -> T) {

    private var graph: T? = null

    internal fun initialize(dependencies: PageGraphDependencies) {
        graph = initializer(dependencies)
    }

    val value: T
        get() = graph ?: error("PageGraphCreator is not initialized. Did you call initialize?")
}

fun <Root : Page<*>, ServiceProvider, ExtraDependencies, Graph : Any> Root.graphCreator(
    context: Context,
    factory: PageGraphFactory<Root, ServiceProvider, ExtraDependencies, Graph>,
    extraDependencies: ExtraDependencies,
): PageGraphCreator<Graph> {
    return PageGraphCreator { _ ->
        throw UnsupportedOperationException(
            "PageGraphCreator is not supported in the JVM box test. Check the sample project about how it works."
        )
    }
}
