// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
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
