package com.bandlab.metro.extensions

import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object MetroExtensionsConfigurationKeys {

    const val OPTION_CONTRIBUTES_INJECTOR_BASELINE = "contributesInjectorBaseline"

    /**
     * A baseline of features that are allowed to use @ContributesInjector.
     */
    val CONTRIBUTES_INJECTOR_BASELINE: CompilerConfigurationKey<Set<String>> =
        CompilerConfigurationKey.create(OPTION_CONTRIBUTES_INJECTOR_BASELINE)
}
