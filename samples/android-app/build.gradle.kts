plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.bandlab.metro.station.sample"

    defaultConfig {
        applicationId = "com.bandlab.metro.station.sample"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes { release { isMinifyEnabled = false } }

    compileOptions {
        val javaVersion = libs.versions.jvmTarget.get().let(JavaVersion::toVersion)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
}