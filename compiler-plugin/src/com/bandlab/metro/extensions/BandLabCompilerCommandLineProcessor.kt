package com.bandlab.metro.extensions

import com.bandlab.metro.extensions.MetroExtensionsConfigurationKeys.OPTION_CONTRIBUTES_INJECTOR_BASELINE
import com.fueledbycaffeine.autoservice.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService
public class BandLabCompilerCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String get() = BuildConfig.KOTLIN_PLUGIN_ID
    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = OPTION_CONTRIBUTES_INJECTOR_BASELINE,
            valueDescription = "A string that represents a set of fully qualified class names, separated by colon",
            description = "Fully qualified class names to use the deprecated ContributesInjector feature",
            required = false,
        ),
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            OPTION_CONTRIBUTES_INJECTOR_BASELINE -> configuration.put(
                MetroExtensionsConfigurationKeys.CONTRIBUTES_INJECTOR_BASELINE,
                value.split(":").toSet()
            )

            else -> error("Unexpected config option: '${option.optionName}'")
        }
    }
}
