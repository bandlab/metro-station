package com.bandlab.common.android.pager.screen

import androidx.lifecycle.LifecycleOwner
import com.bandlab.android.common.activity.CommonActivity
import com.bandlab.common.android.di.HasServiceProvider
import com.bandlab.common.android.di.resolveServiceProvider
import com.bandlab.common.android.pager.screen.di.NavPageDependencies
import com.bandlab.common.android.pager.screen.di.PageGraphCreator
import com.bandlab.common.android.pager.screen.di.PageGraphDependencies
import com.bandlab.common.android.pager.screen.di.PageInjector
import com.bandlab.common.android.pager.screen.di.PageInjectorProvider
import com.bandlab.uikit.api.page.Page

/**
 * Create a [PageInjector] that has a lifecycle matching with host
 */
@Suppress("UNCHECKED_CAST")
fun <ViewModel : Any, Param : Any> Page<ViewModel>.createPageInjector(
    initialParam: Param,
    host: CommonActivity<*>,
    lifecycleOwner: LifecycleOwner,
): PageInjector<ViewModel> {
    // Handles @ContributesComponent
    return if (this is HasServiceProvider) {
        val graphCreator = this.javaClass.getDeclaredField("graphCreator")
            .apply { isAccessible = true }
            .get(this) as PageGraphCreator<ViewModel>

        graphCreator.initialize(
            PageGraphDependencies(
                initialParam = initialParam,
                activity = host,
                lifecycleOwner = lifecycleOwner,
            )
        )
        // Injector can be resolved from the Page component itself
        resolve()
    } else {
        // Handles @ContributesInjector
        val injectorFactory = run {
            // Try to find the injector factory from the host activity first
            val injectorFromActivity = try {
                host.resolveServiceProvider<PageInjectorProvider>().dispatchingPageInjector[this::class]
            } catch (_: Exception) {
                null
            }
            injectorFromActivity
            // Fallback to app-level injector if activity injector is not found
                ?: host.applicationContext
                    .resolveServiceProvider<PageInjectorProvider>()
                    .dispatchingPageInjector[this::class]
                ?: error("No page dependencies factory found for $javaClass")
        }
        val pageDependencies = PageGraphDependencies(
            initialParam = initialParam,
            activity = host,
            lifecycleOwner = lifecycleOwner,
        )

        try {
            injectorFactory.javaClass.declaredMethods
                // The factory instance is the parent graph because we use Any in the multibinding, these conditions can
                // find out the create function from the factory in a solid way without keeping anything in proguard.
                .single {
                    it.name == "create" &&
                        it.parameterTypes[0] == javaClass &&
                        it.parameterCount == PAGE_INJECTOR_FACTORY_PARAM_COUNT &&
                        PageInjector::class.java.isAssignableFrom(it.returnType)
                }
                .invoke(
                    injectorFactory,
                    this,
                    pageDependencies,
                    NavPageDependencies(),
                ) as PageInjector<ViewModel>

        } catch (e: Exception) {
            // Rethrow exception with clearer message, we gonna crash the app anyway.
            throw IllegalStateException("Failed to inject $javaClass graph extension", e)
        }
    }
}

private const val PAGE_INJECTOR_FACTORY_PARAM_COUNT = 3