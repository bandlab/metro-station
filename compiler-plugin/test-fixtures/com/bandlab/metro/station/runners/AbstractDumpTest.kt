package com.bandlab.metro.station.runners

import com.bandlab.metro.station.services.configureImports
import com.bandlab.metro.station.services.configurePlugin
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.runners.ir.AbstractFirLightTreeJvmIrTextTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

/**
 * Dump test that produces FIR golden files (`.fir.txt`) and Kotlin-like IR dump (`.fir.kt.txt`).
 */
open class AbstractDumpTest : AbstractFirLightTreeJvmIrTextTest() {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)

        with(builder) {
            configurePlugin()
            configureImports(addTestImports = false)

            defaultDirectives {
                JvmEnvironmentConfigurationDirectives.JVM_TARGET.with(JvmTarget.JVM_11)
                +ConfigurationDirectives.WITH_STDLIB
                +JvmEnvironmentConfigurationDirectives.FULL_JDK

                +FirDiagnosticsDirectives.FIR_DUMP
                +FirDiagnosticsDirectives.DISABLE_GENERATED_FIR_TAGS

                +CodegenTestDirectives.IGNORE_DEXING
                +CodegenTestDirectives.DUMP_KT_IR
                -CodegenTestDirectives.DUMP_IR
            }
        }
    }
}
