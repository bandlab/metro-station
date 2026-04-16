package com.bandlab.metro.extensions

import com.fueledbycaffeine.autoservice.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService
public class BandLabCompilerCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String
        get() = BuildConfig.KOTLIN_PLUGIN_ID
    override val pluginOptions: Collection<CliOption>
        get() = emptyList()

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        error("Unexpected config option: '${option.optionName}'")
    }
}
