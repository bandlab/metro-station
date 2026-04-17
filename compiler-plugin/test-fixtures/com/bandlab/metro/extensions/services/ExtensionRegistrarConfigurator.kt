package com.bandlab.metro.extensions.services

import com.bandlab.metro.extensions.BandLabPluginComponentRegistrar
import dev.zacsweers.metro.compiler.MetroCommandLineProcessor
import dev.zacsweers.metro.compiler.MetroCompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

fun TestConfigurationBuilder.configurePlugin() {
    useConfigurators(::ExtensionRegistrarConfigurator)
    configureAnnotations()
    configureMetroRuntime()
}

fun TestConfigurationBuilder.configureMetroImports() {
    useSourcePreprocessor(::MetroImportsPreprocessor)
}

fun TestConfigurationBuilder.configureKotlinTestImports() {
    useSourcePreprocessor(::KotlinTestImportsPreprocessor)
}

private class ExtensionRegistrarConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    private val bandLabRegistrar = BandLabPluginComponentRegistrar()
    private val metroRegistrar = MetroCompilerPluginRegistrar()
    private val metroCliProcessor = MetroCommandLineProcessor()

    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        // Configure Metro options from directives before registering
        if (MetroDirectives.GENERATE_CONTRIBUTION_HINTS_IN_FIR in module.directives) {
            val option = metroCliProcessor.pluginOptions.first {
                it.optionName == "generate-contribution-hints-in-fir"
            }
            metroCliProcessor.processOption(option, "true", configuration)
        }

        with(bandLabRegistrar) { registerExtensions(configuration) }
        with(metroRegistrar) { registerExtensions(configuration) }
    }
}
