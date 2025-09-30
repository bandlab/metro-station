@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.binaryCompatibilityValidator)
}

kotlin {
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    jvm()

    applyDefaultHierarchyTemplate()
}
