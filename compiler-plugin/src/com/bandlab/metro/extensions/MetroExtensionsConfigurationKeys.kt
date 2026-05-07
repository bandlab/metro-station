package com.bandlab.metro.extensions

import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal object MetroExtensionsConfigurationKeys {
    val CONTRIBUTES_INJECTOR_BASELINE: CompilerConfigurationKey<Set<String>> =
        CompilerConfigurationKey.create("contributesInjectorBaseline")
}
