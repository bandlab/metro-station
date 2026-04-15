package com.bandlab.compiler.services

import com.bandlab.compiler.BandLabPluginComponentRegistrar
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
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        with(bandLabRegistrar) { registerExtensions(configuration) }
        with(metroRegistrar) { registerExtensions(configuration) }
    }
}
