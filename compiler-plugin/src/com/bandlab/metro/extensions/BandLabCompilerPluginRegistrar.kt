package com.bandlab.metro.extensions

import com.bandlab.metro.extensions.checker.MetroExtensionsFirCheckers
import com.bandlab.metro.extensions.component.ContributesComponentSupertypeGenerator
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension

public class BandLabCompilerPluginRegistrar(
    private val includeBaselineChecker: Boolean,
    private val contributesInjectorBaseline: Set<String>,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirAdditionalCheckersExtension.Factory { session ->
            MetroExtensionsFirCheckers(session, includeBaselineChecker, contributesInjectorBaseline)
        }
        +FirSupertypeGenerationExtension.Factory(::ContributesComponentSupertypeGenerator)
    }
}
