import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.binaryCompatibilityValidator) apply false
}

allprojects {
    group = "com.bandlab.metro.station"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    pluginManager.withPlugin("java") {
        configure<JavaPluginExtension> {
            toolchain { languageVersion.set(libs.versions.jdk.map(JavaLanguageVersion::of)) }
        }
        tasks.withType<JavaCompile>().configureEach {
            options.release.set(libs.versions.jvmTarget.map(String::toInt))
        }
    }

    plugins.withType<KotlinBasePlugin> {
        project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions {
                progressiveMode.set(true)
                if (this is KotlinJvmCompilerOptions) {
                    jvmTarget.set(libs.versions.jvmTarget.map(JvmTarget::fromTarget))
                    freeCompilerArgs.addAll("-Xjvm-default=all")
                }
            }
        }
        if ("samples" !in project.path) {
            configure<KotlinProjectExtension> { explicitApi() }
        }
    }
}