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

rootProject.name = "bandlab-android-kotlin-compiler-plugin"

include("compiler-plugin")
include("gradle-plugin")
include("plugin-annotations")
include("stubs")
