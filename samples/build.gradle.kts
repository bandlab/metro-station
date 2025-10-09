import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.metro) apply false
    id("com.bandlab.metro.station") apply false
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
        project.tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                progressiveMode.set(true)
                jvmTarget.set(libs.versions.jvmTarget.map(JvmTarget::fromTarget))
                freeCompilerArgs.addAll(
                    "-Xjvm-default=all",
                    "-Xcompiler-plugin-order=com.bandlab.metro.station>dev.zacsweers.metro.compiler",
                )
            }
        }
    }
}