import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.gradle.plugin)
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

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("resources"))
    }
    test {
        java.setSrcDirs(listOf("test"))
        resources.setSrcDirs(listOf("testResources"))
    }
}

dependencies {
    implementation(libs.kotlin.gradle.plugin.api)
    testImplementation(libs.kotlin.test.junit5)
}

buildConfig {
    packageName(project.group.toString())

    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")

    val pluginProject = project(":compiler-plugin")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${pluginProject.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${pluginProject.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${pluginProject.version}\"")

    val annotationsProject = project(":plugin-annotations")
    buildConfigField(
        type = "String",
        name = "ANNOTATIONS_LIBRARY_COORDINATES",
        expression = "\"${annotationsProject.group}:${annotationsProject.name}:${annotationsProject.version}\""
    )
}

gradlePlugin {
    plugins {
        register(rootProject.group.toString()) {
            displayName = "MetroStationGraphPlugin"
            description = "A gradle plugin to configure Metro Station plugin for BandLab Android apps."
            implementationClass = "com.bandlab.metro.station.MetroStationGradlePlugin"
        }
    }
}
