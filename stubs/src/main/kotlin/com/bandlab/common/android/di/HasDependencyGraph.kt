package com.bandlab.common.android.di

import android.content.Context
import com.bandlab.android.common.activity.CommonActivity

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
    return when {
        this is CommonActivity<*> -> this.resolve()
        else -> error("Fail to resolve ServiceProvider from Context")
    }
}