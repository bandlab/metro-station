package com.bandlab.android.common.activity

import android.content.ContextWrapper
import com.bandlab.common.android.di.HasDependencyGraph
import com.bandlab.common.di.GeneratedByMetroStation

abstract class CommonActivity<Params : Any> : ContextWrapper(), HasDependencyGraph {

    lateinit var params: Params

    val type: String = "CommonActivity"

    /**
     * Test-only hook so JVM box tests can wire a real graph instance into the activity before
     * calling [inject]. Production code obtains the graph from `applicationContext` instead.
     */
    var graph: Any = Unit

    override fun <T> resolve(): T = HasDependencyGraph.resolveFrom(graph)

    @GeneratedByMetroStation
    open fun inject() {
        throw UnsupportedOperationException("This method will be implemented by the compiler plugin.")
    }

    @OptIn(GeneratedByMetroStation::class)
    fun setAppGraphAndInject(graph: Any) {
        this.graph = graph
        inject()
    }

    interface ServiceProvider
}
