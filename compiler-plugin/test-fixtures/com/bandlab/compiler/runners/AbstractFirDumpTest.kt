package com.bandlab.metro.extensions.runners

import com.bandlab.metro.extensions.services.configureMetroImports
import com.bandlab.metro.extensions.services.configurePlugin
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.runners.ir.AbstractFirLightTreeJvmIrTextTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.junit.jupiter.api.Assumptions

/**
 * Dump test that always produces FIR golden files (`.fir.txt`). Test files can opt in to IR dumping
 * by adding `// DUMP_KT_IR` at the top, which produces an additional `.fir.kt.txt` golden file with
 * a Kotlin-like IR representation. Uses `DUMP_KT_IR` (not `DUMP_IR`) to produce portable output
 * without machine-specific temp paths, matching Metro's own test convention.
 *
 * Dump tests are skipped on preview Kotlin builds (e.g., `-Pkotlin.version=2.3.20-RC`) because both
 * FIR and IR output vary between compiler versions and the golden files only match the version
 * defined in `libs.versions.toml`.
 */
open class AbstractFirDumpTest : AbstractFirLightTreeJvmIrTextTest() {
  override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
    return EnvironmentBasedStandardLibrariesPathProvider
  }

  override fun configure(builder: TestConfigurationBuilder) {
    super.configure(builder)

    with(builder) {
      configurePlugin()
      configureMetroImports()

      defaultDirectives {
        JvmEnvironmentConfigurationDirectives.JVM_TARGET.with(JvmTarget.JVM_11)
        +ConfigurationDirectives.WITH_STDLIB
        +JvmEnvironmentConfigurationDirectives.FULL_JDK

        +FirDiagnosticsDirectives.FIR_DUMP
        +FirDiagnosticsDirectives.DISABLE_GENERATED_FIR_TAGS

        +CodegenTestDirectives.IGNORE_DEXING
      }
    }
  }

  override fun runTest(filePath: String) {
    // Skip dump tests on preview Kotlin builds — golden files only match the catalog version.
    Assumptions.assumeFalse(IS_PREVIEW_KOTLIN_BUILD, "Dump tests skipped on preview Kotlin builds")
    super.runTest(filePath)
  }
}

private val IS_PREVIEW_KOTLIN_BUILD: Boolean =
  System.getProperty("squareMetroExtensions.previewKotlinBuild") == "true"
