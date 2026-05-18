package com.bandlab.common.android.di

import android.content.Context

interface HasServiceProvider {

    fun <T> resolve(): T {
        throw UnsupportedOperationException(
            "resolve is not supported in the JVM box test. Check the sample project about how it works."
        )
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
    throw UnsupportedOperationException(
        "resolveServiceProvider is not supported in the JVM box test. Check the sample project about how it works."
    )
}