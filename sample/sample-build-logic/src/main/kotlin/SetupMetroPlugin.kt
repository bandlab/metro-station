import dev.zacsweers.metro.gradle.DiagnosticSeverity
import dev.zacsweers.metro.gradle.MetroPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class SetupMetroPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(project.libs.plugins.metro.get().pluginId)
        project.pluginManager.apply("com.bandlab.metro.station")

        project.extensions.configure<MetroPluginExtension> {
            // Do not report warning on unused inputs for the sample project
            unusedGraphInputsSeverity.set(DiagnosticSeverity.NONE)
        }

        project.tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                freeCompilerArgs.add(
                    // Our compiler needs to run before metro during the IR phase.
                    "-Xcompiler-plugin-order=com.bandlab.metro.station>dev.zacsweers.metro.compiler",
                )
            }
        }
    }
}