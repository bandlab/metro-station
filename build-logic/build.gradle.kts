plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("com.bandlab.metro.extensions.settings") {
            implementationClass = "com.bandlab.metro.extensions.settings.SettingsPlugin"
        }
    }
}
