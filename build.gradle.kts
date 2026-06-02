import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.binary.compatibility.validator) apply false
    alias(libs.plugins.autoservice) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.metro) apply false
}

allprojects {
    group = project.property("GROUP") as String
    val versionName = project.property("VERSION_NAME") as String
    val metroVersion = libs.versions.metro.get()
    // The version name is consist of "${metro-station}-$metro"
    version = "$versionName-$metroVersion"
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
    plugins.withId("com.vanniktech.maven.publish") {
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "BandLab"
                    url = uri("https://artifactory.bandlab.io/artifactory/libs-release-local")
                    credentials(PasswordCredentials::class)
                }
            }
        }
    }
}