import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class SampleLibPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(project.libs.plugins.android.library.get().pluginId)
        project.pluginManager.apply(project.libs.plugins.kotlin.compose.get().pluginId)
        project.apply<SetupMetroPlugin>()

        project.extensions.configure<LibraryExtension> {
            compileSdk = project.libs.versions.targetAndroidSdk.get().toInt()

            defaultConfig {
                minSdk = project.libs.versions.minAndroidSdk.get().toInt()
            }

            buildFeatures {
                compose = true
            }
        }
    }
}