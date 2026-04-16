plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.binary.compatibility.validator) apply false
    alias(libs.plugins.autoservice) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.metro) apply false
}

allprojects {
    group = "com.bandlab.compiler"
    version = "0.0.1"
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "BandLab"
                    url = uri("https://artifactory.bandlab.io/artifactory/libs-release-local")
                    credentials(PasswordCredentials::class)
                }
            }
        }
    }
}