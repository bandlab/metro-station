package com.bandlab.common.android.di

import kotlin.reflect.KClass

/**
 * This annotation generates a Metro Dependency Graph for the feature.
 * It covers the responsibility of [ContributesInjector], so the target itself can be injected
 * without any additional changes. An example of activity should look like this:
 *
 * ```kotlin
 * @ContributesComponent(appDependencies = MyActivity.ServiceProvider::class)
 * class MyActivity : CommonActivity<Unit>(), HasServiceProvider {
 *
 *     private val graph by componentCreator(createGraphFactory<MyActivityGraph.Factory>())
 *
 *     override fun <T> resolve(): T = HasServiceProvider.resolveFrom(graph)
 *
 *     interface ServiceProvider {
 *         val myDependency: MyDependency
 *     }
 * }
 * ```
 *
 * @param appDependencies The service provider that you need from the App graph.
 * @param graphMarker A marker to aggregate a graph, by default it's the component target itself (ex. activity).
 * @param extraDependencies Optional service providers that you need from other feature graphs.
 */
@Target(AnnotationTarget.CLASS)
public annotation class ContributesComponent(
    val appDependencies: KClass<*>,
    val graphMarker: KClass<*> = Nothing::class,
    vararg val extraDependencies: KClass<*> = [],
)