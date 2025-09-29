package com.bandlab.metro.station.runners

import com.bandlab.metro.station.services.ExtensionRegistrarConfigurator
import com.bandlab.metro.station.services.PluginAnnotationsProvider
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.runners.AbstractFirPhasedDiagnosticTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class AbstractJvmDiagnosticTest : AbstractFirPhasedDiagnosticTest(FirParser.LightTree) {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)

        with(builder) {
            /*
             * Containers of different directives, which can be used in tests:
             * - ModuleStructureDirectives
             * - LanguageSettingsDirectives
             * - DiagnosticsDirectives
             * - FirDiagnosticsDirectives
             *
             * All of them are located in `org.jetbrains.kotlin.test.directives` package
             */
            defaultDirectives {
                +JvmEnvironmentConfigurationDirectives.FULL_JDK

                +CodegenTestDirectives.IGNORE_DEXING // Avoids loading R8 from the classpath.
            }

            useConfigurators(
                ::PluginAnnotationsProvider,
                ::ExtensionRegistrarConfigurator
            )

            useSourcePreprocessor(
                ::MetroStationDefaultImportPreprocessor
            )
        }
    }
}
