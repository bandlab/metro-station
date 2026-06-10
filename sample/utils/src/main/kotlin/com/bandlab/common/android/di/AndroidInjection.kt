package com.bandlab.common.android.di

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object AndroidInjection {

    fun <T : FragmentActivity> inject(activity: T) {
        resolveInjectorAndInject(activity)
    }

    fun <T : Fragment> inject(fragment: T) {
        resolveInjectorAndInject(fragment)
    }

    /**
     * Try to resolve the injector from the component itself in case if it's a standalone component, if we cannot
     * find the injector, we fallback to the app graph and try to find if the injector is available there.
     */
    @Suppress("UNCHECKED_CAST")
    private fun resolveInjectorAndInject(target: Any) {
        if (target is HasDependencyGraph) {
            val injector = try {
                target.resolve<MembersInjectorProvider<Any>>().injector
            } catch (_: Exception) {
                // Fail to inject as a standalone component, continue to resolve injector from other places.
                null
            }

            if (injector != null) {
                injector.injectMembers(target)
                return
            }
        }

        val targetContext = when (target) {
            is FragmentActivity -> target
            is Fragment -> target.requireContext()
            else -> error("Not supported type ${target::class}")
        }

        var graphExtensionFactory: Any?
        graphExtensionFactory = target.resolveFactoryFrom(targetContext)
            ?: target.resolveFactoryFrom(targetContext.applicationContext)

        // If it's a fragment, try to resolve factory from the activity
        if (graphExtensionFactory == null && target is Fragment) {
            graphExtensionFactory = target.activity?.let { target.resolveFactoryFrom(it) }
        }

        if (graphExtensionFactory == null) {
            error(
                buildString {
                    append("No injector found for ${target::class.qualifiedName}, tried to find in AppScope")
                    appendLine(".")
                    if (target is Fragment) {
                        append("Also tried to inject fragment in activity ")
                        appendLine("${target.activity} (HasServiceProvider=${target.activity is HasDependencyGraph})")
                    }
                    append("You probably forgot to annotate it with @StationEntry, ")
                    appendLine("or the scope of the @StationEntry is not correct.")
                }
            )
        }

        val graphExtension = try {
            graphExtensionFactory.javaClass.declaredMethods
                // The factory instance is the parent graph because we use Any in the multibinding, these conditions can
                // find out the create function from the factory in a solid way without keeping anything in proguard.
                .single {
                    it.name == "create" &&
                        it.parameterCount == 1 &&
                        it.parameterTypes.first() == target.javaClass &&
                        MembersInjectorProvider::class.java.isAssignableFrom(it.returnType)
                }
                .invoke(graphExtensionFactory, target)
        } catch (e: Exception) {
            // Rethrow exception with clearer message, we gonna crash the app anyway.
            throw IllegalStateException("Failed to inject ${target.javaClass} graph extension", e)
        }

        (graphExtension as MembersInjectorProvider<Any>)
            .injector
            .injectMembers(target)
    }

    private fun Any.resolveFactoryFrom(context: Context): Any? {
        if (context !is HasDependencyGraph) return null
        return try {
            context.resolve<GraphExtensionFactoriesProvider>()
                .graphExtensionFactories[this::class]
        } catch (_: Exception) {
            null
        }
    }
}