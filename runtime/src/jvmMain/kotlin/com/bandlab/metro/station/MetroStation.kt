package com.bandlab.metro.station

import kotlin.reflect.KClass

/**
 * Marks an entry point of a class that you want to perform members injection by a standalone Dependency Graph.
 *
 * We generate Metro's [@DependencyGraph](https://github.com/ZacSweers/metro/blob/main/runtime/src/commonMain/kotlin/dev/zacsweers/metro/DependencyGraph.kt)
 * under the hood.
 *
 * For known Android Components, such as Activity, Fragments, Views, Services, and Broadcast Receivers, metro-station
 * will generate an injection point automatically for you.
 *
 * @property scope The scope this metro station aggregates, by default the scope will be the target itself.
 * @property additionalScopes Additional scopes this metro station aggregates. [scope] must be
 *   defined if this is defined, as this property is purely for convenience.
 * @property excludes Optional list of excluded contributing classes (requires a [scope] to be
 *   defined).
 * @property bindingContainers Optional list of included binding containers. See the doc on
 *   [BindingContainer](https://github.com/ZacSweers/metro/blob/main/runtime/src/commonMain/kotlin/dev/zacsweers/metro/BindingContainer.kt) for more details.
 * TODO: more params after finalizing tech design
 */
public annotation class MetroStation(
    val scope: KClass<*> = Nothing::class,
    val additionalScopes: Array<KClass<*>> = [],
    val excludes: Array<KClass<*>> = [],
    val bindingContainers: Array<KClass<*>> = [],
)
