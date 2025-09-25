plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.binaryCompatibilityValidator) apply false
}

allprojects {
    group = "com.bandlab.metro.station"
    version = "0.1.0-SNAPSHOT"
}
