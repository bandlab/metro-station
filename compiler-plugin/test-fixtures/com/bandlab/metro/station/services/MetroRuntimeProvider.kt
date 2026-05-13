package com.bandlab.metro.station.services

import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

fun TestConfigurationBuilder.configureMetroRuntime() {
    useConfigurators(::MetroRuntimeConfigurator)
    useCustomRuntimeClasspathProviders(::MetroRuntimeClasspathProvider)
}

private class MetroRuntimeConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        configuration.addJvmClasspathRoots(metroRuntimeClasspath)
    }
}

private class MetroRuntimeClasspathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
    override fun runtimeClassPaths(module: TestModule): List<File> = metroRuntimeClasspath
}

private val metroRuntimeClasspath: List<File> by lazy {
    val property = System.getProperty("metroRuntime.classpath")
        ?: error("Unable to get a valid classpath from 'metroRuntime.classpath' property")
    property.split(File.pathSeparator).map(::File)
}
