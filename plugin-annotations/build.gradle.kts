plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.binary.compatibility.validator)
}

kotlin {
    explicitApi()
}
