pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "metro-station"

include(
    ":compiler-plugin",
    ":gradle-plugin",
    ":runtime"
)
