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
            compileSdk = project.libs.versions.targetAndroidSdk.get().toInt()

            defaultConfig {
                applicationId = "com.bandlab.metro.station.sampleapp"
                minSdk = project.libs.versions.minAndroidSdk.get().toInt()
                targetSdk = project.libs.versions.targetAndroidSdk.get().toInt()
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

            buildFeatures {
                compose = true
            }
        }
    }
}