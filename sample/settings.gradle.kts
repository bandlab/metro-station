pluginManagement {
    includeBuild("../build-logic")
}

plugins {
    id("com.bandlab.metro.station.settings")
}

rootProject.name = "metro-station-sample"
include(":app")

includeBuild("..")
