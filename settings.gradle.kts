pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "metro-extensions"

include("compiler-plugin")
include("gradle-plugin")
include("plugin-annotations")
include("stubs")
