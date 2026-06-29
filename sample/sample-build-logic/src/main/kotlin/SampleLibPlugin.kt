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
            compileSdk = 37

            defaultConfig {
                minSdk = 24
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }

            buildFeatures {
                compose = true
            }
        }
    }
}