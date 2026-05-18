import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class SampleAppPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(project.libs.plugins.android.application.get().pluginId)
        project.pluginManager.apply(project.libs.plugins.kotlin.compose.get().pluginId)
        project.apply<SetupMetroPlugin>()

        project.extensions.configure<ApplicationExtension> {
            namespace = "com.bandlab.metro.station.sample"
            compileSdk = 36

            defaultConfig {
                applicationId = "com.bandlab.metro.station.sampleapp"
                minSdk = 24
                targetSdk = 36
                versionCode = 1
                versionName = "1.0"

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
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