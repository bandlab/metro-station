pluginManagement {
    includeBuild("sample-build-logic")
    includeBuild("../build-logic")
}

plugins {
    id("com.bandlab.metro.station.settings")
}

rootProject.name = "metro-station-sample"
include(":app")
include(":profile")
include(":utils")

includeBuild("..")
