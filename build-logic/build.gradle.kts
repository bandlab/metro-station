plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("com.bandlab.metro.station.settings") {
            implementationClass = "com.bandlab.metro.station.settings.SettingsPlugin"
        }
    }
}
