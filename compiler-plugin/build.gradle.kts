@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8EnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8Plugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.autoservice)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.gradle.java.test.fixtures)
    alias(libs.plugins.gradle.idea)
}

project.plugins.apply(D8Plugin::class.java)

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(listOf("resources"))
    }
    testFixtures {
        java.setSrcDirs(listOf("test-fixtures"))
    }
    test {
        java.setSrcDirs(listOf("test", "test-gen"))
        resources.setSrcDirs(listOf("testData"))
    }
}

idea {
    module.generatedSourceDirs.add(projectDir.resolve("test-gen"))
}

val testArtifacts: Configuration by configurations.creating

val annotationsRuntimeClasspath by configurations.dependencyScope("annotationsRuntimeClasspath") {
    isTransitive = false
}
val annotationsJvmRuntimeClasspath by configurations.resolvable("annotationsJvmRuntimeClasspath") {
    extendsFrom(annotationsRuntimeClasspath)
}

val metroRuntimeClasspath by configurations.dependencyScope("metroRuntimeClasspath") {
    isTransitive = false
}
val metroRuntimeResolvable by configurations.resolvable("metroRuntimeResolvable") {
    extendsFrom(metroRuntimeClasspath)
}

dependencies {
    compileOnly(libs.kotlin.compiler)
    compileOnly(libs.metro.compiler)

    testFixturesApi(libs.kotlin.test.junit5)
    testFixturesApi(libs.kotlin.test.framework)
    testFixturesApi(libs.kotlin.compiler)
    testFixturesApi(libs.metro.compiler)
    testFixturesRuntimeOnly(libs.junit)

    annotationsRuntimeClasspath(project(":plugin-annotations"))
    annotationsRuntimeClasspath(project(":stubs"))

    // Dependencies required to run the internal test framework.
    testArtifacts(libs.kotlin.stdlib)
    testArtifacts(libs.kotlin.stdlib.jdk8)
    testArtifacts(libs.kotlin.reflect)
    testArtifacts(libs.kotlin.test)
    testArtifacts(libs.kotlin.script.runtime)
    testArtifacts(libs.kotlin.annotations.jvm)

    // Metro runtime for running Metro tests
    metroRuntimeClasspath(libs.metro.runtime)
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }

    packageName(group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")
}

tasks.test {
    dependsOn(testArtifacts)
    dependsOn(annotationsJvmRuntimeClasspath)
    dependsOn(metroRuntimeResolvable)

    useJUnitPlatform()
    workingDir = rootDir

    systemProperty("annotationsRuntime.jvm.classpath", annotationsJvmRuntimeClasspath.asPath)
    systemProperty("metroRuntime.classpath", metroRuntimeResolvable.asPath)

    // Properties required to run the internal test framework.
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")

    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", rootDir)

    // Properties required to run JS tests from the internal test framework.
    val d8EnvSpec = project.the<D8EnvSpec>()
    with(d8EnvSpec) { dependsOn(project.d8SetupTaskProvider) }
}

kotlin {
    explicitApi()

    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}

val generateTests by tasks.registering(JavaExec::class) {
    inputs.dir(layout.projectDirectory.dir("testData"))
        .withPropertyName("testData")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(layout.projectDirectory.dir("test-gen"))
        .withPropertyName("generatedTests")

    classpath = sourceSets.testFixtures.get().runtimeClasspath
    mainClass.set("com.bandlab.compiler.GenerateTestsKt")
    workingDir = rootDir
}

tasks.compileTestKotlin {
    dependsOn(generateTests)
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = testArtifacts.files
        .find { """$jarName-\d.*""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}
