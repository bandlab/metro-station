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
        get() = listOf(
            CliOption(
                optionName = "contributesInjectorBaseline",
                valueDescription = "A set of strings",
                description = "Fully qualified class name to include in the ContributesInjector baseline",
                required = false,
                allowMultipleOccurrences = true,
            ),
        )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            "contributesInjectorBaseline" -> {
                val current =
                    configuration.get(MetroExtensionsConfigurationKeys.CONTRIBUTES_INJECTOR_BASELINE).orEmpty()
                configuration.put(MetroExtensionsConfigurationKeys.CONTRIBUTES_INJECTOR_BASELINE, current + value)
            }

            else -> error("Unexpected config option: '${option.optionName}'")
        }
    }
}
