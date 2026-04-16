package com.bandlab.metro.extensions.runners

import com.bandlab.metro.extensions.services.configureKotlinTestImports
import com.bandlab.metro.extensions.services.configureMetroImports
import com.bandlab.metro.extensions.services.configurePlugin
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.runners.codegen.AbstractFirBlackBoxCodegenTestBase
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class AbstractBoxTest : AbstractFirBlackBoxCodegenTestBase(FirParser.LightTree) {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }

    override fun configure(builder: TestConfigurationBuilder) = with(builder) {
        super.configure(this)
        /*
         * Containers of different directives, which can be used in tests:
         * - ModuleStructureDirectives
         * - LanguageSettingsDirectives
         * - DiagnosticsDirectives
         * - FirDiagnosticsDirectives
         * - CodegenTestDirectives
         * - JvmEnvironmentConfigurationDirectives
         *
         * All of them are located in `org.jetbrains.kotlin.test.directives` package
         */
        defaultDirectives {
            JvmEnvironmentConfigurationDirectives.JVM_TARGET.with(JvmTarget.JVM_11)
            +ConfigurationDirectives.WITH_STDLIB
            +JvmEnvironmentConfigurationDirectives.FULL_JDK

            +CodegenTestDirectives.IGNORE_DEXING // Avoids loading R8 from the classpath.
        }

        configurePlugin()
        configureMetroImports()
        configureKotlinTestImports()
    }
}
