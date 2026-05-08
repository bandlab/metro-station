package com.bandlab.metro.extensions

import com.bandlab.metro.extensions.component.ContributesComponentIr
import com.bandlab.metro.extensions.injector.ContributesInjectorIr
import com.fueledbycaffeine.autoservice.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@AutoService
public class BandLabPluginComponentRegistrar : CompilerPluginRegistrar() {

    private companion object {
        val isIde by lazy {
            try {
                // Try to look up an IntelliJ-only class
                Class.forName("org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession")
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
    }

    override val pluginId: String get() = BuildConfig.KOTLIN_PLUGIN_ID
    override val supportsK2: Boolean get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val contributesInjectorBaseline =
            configuration.get(MetroExtensionsConfigurationKeys.CONTRIBUTES_INJECTOR_BASELINE)
        FirExtensionRegistrarAdapter.registerExtension(
            BandLabCompilerPluginRegistrar(
                includeBaselineChecker = contributesInjectorBaseline != null,
                contributesInjectorBaseline = contributesInjectorBaseline.orEmpty()
            )
        )

        // Do not run IR extensions in IDE
        if (!isIde) {
            IrGenerationExtension.registerExtension(ContributesComponentIr())
            IrGenerationExtension.registerExtension(ContributesInjectorIr())
        }
    }
}
