package com.bandlab.metro.extensions

import com.bandlab.metro.extensions.component.ContributesComponentIr
import com.fueledbycaffeine.autoservice.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

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
        if (!isIde) {
            IrGenerationExtension.registerExtension(ContributesComponentIr())
        }
    }
}
