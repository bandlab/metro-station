pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.settings") version "8.12.3"
    }
}

dependencyResolutionManagement {
    versionCatalogs { maybeCreate("libs").apply { from(files("../gradle/libs.versions.toml")) } }
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    id("com.android.settings") version "8.12.3"
}

android {
    compileSdk = 36
    targetSdk = 36
    minSdk = 28
}

rootProject.name = "metro-station-samples"

include(":android-app")

includeBuild("..")