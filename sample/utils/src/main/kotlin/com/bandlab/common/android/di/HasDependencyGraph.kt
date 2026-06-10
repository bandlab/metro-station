package com.bandlab.common.android.di

import android.app.Activity
import android.content.Context

/**
 *  Generic service provider resolver, before resolving it, you need to
 *  contribute your service provider towards the desired graph.
 *
 *  For example:
 *  ```kotlin
 *  @ContributesTo(AppScope::class)
 *  interface MyServiceProvider {
 *      val appDependency: AppDependency
 *  }
 *
 *  fun resolveProvider() {
 *      applicationContext.resolveServiceProvider<MyServiceProvider>()
 *  }
 *  ```
 */
interface HasDependencyGraph {

    fun <T> resolve(): T {
        throw UnsupportedOperationException(
            "This should be implemented by the compiler, unless you manually extend this supertype."
        )
    }

    companion object {

        fun <T> resolveFrom(graph: Any): T = try {
            @Suppress("UNCHECKED_CAST")
            graph as T
        } catch (e: Exception) {
            throw IllegalStateException(
                """
                Fail to cast from the graph, make sure you contribute your provider correctly.
                See: HasDependencyGraph kdoc for more details
                """.trimIndent(),
                e
            )
        }
    }
}

fun <T> Context.resolveServiceProvider(): T {
    val app = applicationContext
    return when {
        this is Activity && this is HasDependencyGraph -> this.resolve()
        app is HasDependencyGraph -> app.resolve()
        else -> error("Application doesn't implement HasDependencyGraph interface")
    }
}
