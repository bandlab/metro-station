plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.binary.compatibility.validator)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApi()
}
