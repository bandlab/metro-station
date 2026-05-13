pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.bandlab.metro.station.settings")
}

rootProject.name = "metro-station"

include("compiler-plugin")
include("gradle-plugin")
include("plugin-annotations")
include("stubs")
