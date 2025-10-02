@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.binaryCompatibilityValidator)
}

kotlin {
    androidTarget()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    jvm()

    applyDefaultHierarchyTemplate()
}

android {
    namespace = "com.bandlab.metro.station"
    compileSdk = 36
}
