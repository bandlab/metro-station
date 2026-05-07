package com.bandlab.metro.extensions

import com.bandlab.metro.extensions.checker.MetroExtensionsFirCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

public class BandLabCompilerPluginRegistrar(
    private val includeBaselineChecker: Boolean = true,
    private val contributesInjectorBaseline: Set<String> = emptySet(),
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirAdditionalCheckersExtension.Factory { session ->
            MetroExtensionsFirCheckers(session, includeBaselineChecker, contributesInjectorBaseline)
        }
    }
}
