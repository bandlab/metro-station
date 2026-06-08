package com.bandlab.metro.station

import com.bandlab.metro.station.BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

// Used via reflection.
@Suppress("unused")
public class MetroStationGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("metroStation", MetroStationExtension::class.java)

        val isMetroStrictCompatibility = target.providers
            .gradleProperty("com.bandlab.metro.station.metroStrictCompatibility")
            .map { it.toBoolean() }
            .getOrElse(true)
        if (isMetroStrictCompatibility) {
            var metroVersion: String? = null
            target.plugins.withId("dev.zacsweers.metro") {
                val clazz = Class.forName("dev.zacsweers.metro.gradle.BuildConfigKt")
                val property = clazz.fields.find { it.name == "VERSION" }

                metroVersion = property?.let {
                    it.isAccessible = true
                    it.get(null)?.toString()
                }
            }
            target.afterEvaluate {
                if (BuildConfig.SUPPORTED_METRO_VERSION != metroVersion) {
                    throw GradleException(
                        "Metro Station version (${BuildConfig.KOTLIN_PLUGIN_VERSION}) is incompatible with Metro version ($metroVersion).\n" +
                            "It might work but it's safer to keep them in sync. Please make a new release of metro station."
                    )
                }
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
        version = BuildConfig.KOTLIN_PLUGIN_VERSION,
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        kotlinCompilation.defaultSourceSet.dependencies {
            implementation(ANNOTATIONS_LIBRARY_COORDINATES)
        }

        return project.provider {
            val extension = project.extensions.getByType(MetroStationExtension::class.java)

            buildList {
                extension.stationEntriesBaseline
                    .getOrElse(emptySet())
                    .takeUnless { it.isEmpty() }
                    ?.let { SubpluginOption("stationEntriesBaseline", value = it.joinToString(":")) }
                    ?.let(::add)
            }
        }
    }
}
