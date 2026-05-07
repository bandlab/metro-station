package com.bandlab.metro.extensions

import com.bandlab.metro.extensions.BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

// Used via reflection.
@Suppress("unused")
public class MetroExtensionsGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("metroExtensions", MetroExtensionsGradleExtension::class.java)
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
            val extension = project.extensions.getByType(MetroExtensionsGradleExtension::class.java)

            buildList {
                extension.contributesInjectorBaseline.get().forEach { entry ->
                    add(SubpluginOption(key = "contributesInjectorBaseline", value = entry))
                }
            }
        }
    }
}
