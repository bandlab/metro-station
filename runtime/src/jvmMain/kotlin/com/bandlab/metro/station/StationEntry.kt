package com.bandlab.metro.station

import kotlin.reflect.KClass

/**
 * Marks an entry point of a class where you want to perform members injection by using the parent's graph.
 *
 * We generate Metro's [@GraphExtension](https://github.com/ZacSweers/metro/blob/main/runtime/src/commonMain/kotlin/dev/zacsweers/metro/GraphExtension.kt)
 * under the hood, and contribute the graph extension to [parentScope].
 *
 * For known Android Components, such as Activity, Fragments, Views, Services, and Broadcast Receivers, metro-station
 * will generate an injection point automatically for you.
 *
 * @property parentScope The parent graph's scope this entry will be contributed to.
 * @property scope The scope this station entry aggregates, by default the scope will be the target itself.
 * @property additionalScopes Additional scopes this station entry aggregates. [scope] must be
 *   defined if this is defined, as this property is purely for convenience.
 * @property excludes Optional list of excluded contributing classes (requires a [scope] to be
 *   defined).
 * @property bindingContainers Optional list of included binding containers. See the doc on
 *   [BindingContainer](https://github.com/ZacSweers/metro/blob/main/runtime/src/commonMain/kotlin/dev/zacsweers/metro/BindingContainer.kt) for more details.
 */
@Target(AnnotationTarget.CLASS)
public annotation class StationEntry(
    val parentScope: KClass<*> = Nothing::class,
    val scope: KClass<*> = Nothing::class,
    val additionalScopes: Array<KClass<*>> = [],
    val excludes: Array<KClass<*>> = [],
    val bindingContainers: Array<KClass<*>> = [],
)
