package com.bandlab.metro.station.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.maven
import java.io.File
import java.util.*

class SettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val localProperties = Properties().apply {
            val file = File(settings.rootDir, "local.properties")
            if (file.exists()) {
                file.inputStream().use { load(it) }
            }
        }

        fun getPropertyOrFail(propertyName: String): String {
            return localProperties.getProperty(propertyName)
                ?: settings.providers.gradleProperty(propertyName).orNull
                ?: System.getenv(propertyName)
                ?: error("Property $propertyName not found. Please add it to local.properties or gradle.properties.")
        }

        settings.pluginManagement {
            repositories {
                google()
                if (getPropertyOrFail("USE_MAVEN_LOCAL").toBooleanStrict()) {
                    mavenLocal()
                }
                mavenCentral()
                maven("https://central.sonatype.com/repository/maven-snapshots/")
                if (getPropertyOrFail("USE_INTERNAL_ARTIFACTORY").toBooleanStrict()) {
                    maven {
                        setUrl(getPropertyOrFail("ARTIFACTORY_URL"))
                        credentials {
                            username = getPropertyOrFail("ARTIFACTORY_USERNAME")
                            password = getPropertyOrFail("ARTIFACTORY_PASSWORD")
                        }
                    }
                }
                gradlePluginPortal()
            }
        }

        settings.dependencyResolutionManagement {
            repositories {
                google()
                if (getPropertyOrFail("USE_MAVEN_LOCAL").toBooleanStrict()) {
                    mavenLocal()
                }
                mavenCentral()
                maven("https://central.sonatype.com/repository/maven-snapshots/")
                if (getPropertyOrFail("USE_INTERNAL_ARTIFACTORY").toBooleanStrict()) {
                    maven {
                        setUrl(getPropertyOrFail("ARTIFACTORY_URL"))
                        credentials {
                            username = getPropertyOrFail("ARTIFACTORY_USERNAME")
                            password = getPropertyOrFail("ARTIFACTORY_PASSWORD")
                        }
                    }
                }
            }
        }
    }
}