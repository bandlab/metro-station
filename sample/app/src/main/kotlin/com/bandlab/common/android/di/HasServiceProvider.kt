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
interface HasServiceProvider {

    fun <T> resolve(): T {
        throw UnsupportedOperationException("Implemented by the compiler")
    }

    companion object {

        fun <T> resolveFrom(graph: Any): T = try {
            @Suppress("UNCHECKED_CAST")
            graph as T
        } catch (e: Exception) {
            throw IllegalStateException(
                """
                Fail to cast the service provider, make sure you contribute your provider correctly.
                See: HasServiceProvider kdoc for more details
                """.trimIndent(),
                e
            )
        }
    }
}

fun <T> Context.resolveServiceProvider(): T {
    val app = applicationContext
    return when {
        this is Activity && this is HasServiceProvider -> this.resolve()
        app is HasServiceProvider -> app.resolve()
        else -> error("Application doesn't implement HasServiceProvider interface")
    }
}
