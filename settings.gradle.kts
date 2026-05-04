pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.bandlab.metro.extensions.settings")
}

rootProject.name = "metro-extensions"

include("compiler-plugin")
include("gradle-plugin")
include("plugin-annotations")
include("stubs")
