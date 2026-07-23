import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.binary.compatibility.validator) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.metro) apply false
}

val metroVersion = libs.versions.metro.get()
val kotlinJvmPlugin = libs.plugins.kotlin.jvm.get()
val mavenPublishPlugin = libs.plugins.maven.publish.get()

allprojects {
    group = project.property("GROUP") as String
    val versionName = project.property("VERSION_NAME") as String
    // The version name is consist of "${metro-station}-$metro"
    version = "$versionName-$metroVersion"
}

subprojects {
    plugins.withId(kotlinJvmPlugin.pluginId) {
        configure<KotlinJvmExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(libs.versions.jdk.get()))
            }
        }
        configure<JavaPluginExtension> {
            val javaVersion = JavaVersion.toVersion(libs.versions.jdk.get())
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
    plugins.withId(mavenPublishPlugin.pluginId) {
        // This is to make sure publishing picks up the synthetic version we declared above
        extensions.configure<MavenPublishBaseExtension> {
            coordinates(project.group.toString(), project.name, project.version.toString())
        }
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "BandLab"
                    url = uri("https://artifactory.bandlab.io/artifactory/libs-release")
                    credentials(PasswordCredentials::class)
                }
            }
        }
    }
}