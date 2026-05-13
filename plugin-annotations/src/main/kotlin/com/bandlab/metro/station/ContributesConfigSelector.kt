package com.bandlab.metro.station

/**
 * Contributes the config selector to the app graph and make it visible on the debug screen.
 *
 * ```kotlin
 * @ContributesConfigSelector
 * object MyConfigSelector : BooleanConfigSelector {
 *
 *   // The compiler plugin generates:
 *   @ContributesTo(AppScope::class)
 *   interface MultibindingContribution {
 *     @Binds @IntoSet
 *     fun bind(impl: MyConfigSelector): DebuggableConfigSelector
 *   }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS)
public annotation class ContributesConfigSelector