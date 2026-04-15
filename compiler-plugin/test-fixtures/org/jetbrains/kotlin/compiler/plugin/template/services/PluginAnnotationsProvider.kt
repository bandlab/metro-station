package org.jetbrains.kotlin.compiler.plugin.template.services

import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.targetPlatform
import java.io.File

fun TestConfigurationBuilder.configureAnnotations() {
    useConfigurators(::PluginAnnotationsProvider)
    useCustomRuntimeClasspathProviders(::PluginAnnotationsClasspathProvider)
}

private class PluginAnnotationsProvider(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        val platform = module.targetPlatform(testServices)
        when {
            platform.isJvm() -> {
                configuration.addJvmClasspathRoots(annotationsJvmRuntimeClasspath)
            }

            platform.isJs() -> {
                val libraries = configuration.getList(JSConfigurationKeys.LIBRARIES)
                configuration.put(
                    JSConfigurationKeys.LIBRARIES,
                    libraries + annotationsJsRuntimeClasspath.map { it.absolutePath },
                )
            }
        }
    }
}

private class PluginAnnotationsClasspathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
    override fun runtimeClassPaths(module: TestModule): List<File> {
        val targetPlatform = module.targetPlatform(testServices)
        return when {
            targetPlatform.isJvm() -> annotationsJvmRuntimeClasspath
            targetPlatform.isJs() -> annotationsJsRuntimeClasspath
            else -> emptyList()
        }
    }
}

private val annotationsJvmRuntimeClasspath = classpathFiles("annotationsRuntime.jvm.classpath")
private val annotationsJsRuntimeClasspath = classpathFiles("annotationsRuntime.js.classpath")

private fun classpathFiles(property: String): List<File> {
    val property = System.getProperty(property)
        ?: error("Unable to get a valid classpath from '$property' property")
    return property.split(File.pathSeparator).map(::File)
}
