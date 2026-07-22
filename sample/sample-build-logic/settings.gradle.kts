import java.util.*

val localProperties = Properties().apply {
    val file = File(settingsDir, "../local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val gradleProperties = Properties().apply {
    val file = File(settingsDir, "../gradle.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun getPropertyOrFail(propertyName: String): String {
    return localProperties.getProperty(propertyName)
        ?: gradleProperties.getProperty(propertyName)
        ?: providers.gradleProperty(propertyName).orNull
        ?: System.getenv(propertyName)
        ?: error("Property $propertyName not found. Please add it to local.properties or gradle.properties.")
}

dependencyResolutionManagement {
    repositories {
        google()
        if (getPropertyOrFail("USE_MAVEN_LOCAL").toBooleanStrict()) {
            mavenLocal()
        }
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
        gradlePluginPortal()
        if (getPropertyOrFail("USE_INTERNAL_ARTIFACTORY").toBooleanStrict()) {
            maven {
                setUrl(getPropertyOrFail("ARTIFACTORY_URL"))
                credentials {
                    username = getPropertyOrFail("ARTIFACTORY_USERNAME")
                    password = getPropertyOrFail("ARTIFACTORY_PASSWORD")
                }
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "sample-build-logic"

