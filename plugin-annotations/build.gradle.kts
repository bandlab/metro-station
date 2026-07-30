import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.binary.compatibility.validator)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApi()
    jvmToolchain(libs.versions.jdk.get().toInt())

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.metro.runtime)
}