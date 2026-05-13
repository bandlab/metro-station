package com.bandlab.metro.station.services

import com.bandlab.metro.station.MetroStationPluginRegistrar
import com.bandlab.metro.station.extension.StationEntryIr
import com.bandlab.metro.station.graph.MetroStationIr
import com.bandlab.metro.station.services.MetroDirectives
import dev.zacsweers.metro.compiler.MetroCommandLineProcessor
import dev.zacsweers.metro.compiler.MetroCompilerPluginRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

fun TestConfigurationBuilder.configurePlugin() {
    useDirectives(MetroDirectives)
    useConfigurators(::ExtensionRegistrarConfigurator)
    configureAnnotations()
    configureMetroRuntime()
}

fun TestConfigurationBuilder.configureImports(
    addCommonImports: Boolean,
    addMetroImports: Boolean,
    addTestImports: Boolean
) {
    useSourcePreprocessor(
        { testService ->
            ImportsPreprocessor(
                testService,
                buildSet {
                    if (addCommonImports) {
                        addAll(
                            setOf(
                                "com.bandlab.metro.station.*",
                                "com.bandlab.common.android.di.*",
                                "com.bandlab.common.android.pager.screen.*",
                                "com.bandlab.common.android.pager.screen.di.*",
                                "com.bandlab.android.common.activity.*",
                                "com.bandlab.uikit.api.page.*",
                                "com.bandlab.config.api.*",
                                "android.content.Context"
                            )
                        )
                    }
                    if (addMetroImports) add("dev.zacsweers.metro.*")
                    if (addTestImports) add("kotlin.test.*")
                }
            )
        }
    )
}

private class ExtensionRegistrarConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
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

        val includeBaselineChecker = MetroDirectives.ENABLE_STATION_ENTRIES_BASELINE in module.directives
        FirExtensionRegistrarAdapter.registerExtension(
            MetroStationPluginRegistrar(
                includeBaselineChecker = includeBaselineChecker,
                stationEntriesBaseline = emptySet()
            )
        )
        IrGenerationExtension.registerExtension(MetroStationIr())
        IrGenerationExtension.registerExtension(StationEntryIr())
        with(metroRegistrar) { registerExtensions(configuration) }
    }
}
