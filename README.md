# 🚉 Metro Station

Metro station is a Kotlin compiler plugin built on [Metro][metro] to simplify members injection for Android components.

> **⚠️ This repo is pretty much a wip, there is no release available yet.**

## Usages

There are two main annotations located in `:runtime` - [@MetroStation][metro-station] and 
[@StationEntry][station-entry], they're analogous to metro's [@DependencyGraph][dependency-graph] and 
[@GraphExtension][graph-extension].

### @MetroStation

Annotate `@MetroStation` on a target where you want a standalone dependency graph.

For example:

```kotlin
@MetroStation
class MyActivity

// Generated FIR structure by metro-station
@DependencyGraph(scope = MyActivity::class)
interface MyActivityDependencyGraph {
    fun inject(target: MyActivity)

    @DependencyGraph.Factory
    interface Factory {
        fun create(@Provides target: MyActivity): MyActivityDependencyGraph
    }
}
```

We plan to generate injection points automatically for known Android components, like Activities, Fragments, Views,
Broadcast Receivers and Workers because we know when they should be injected. But this is yet to be implemented.
(see https://github.com/bandlab/metro-station/issues/22)


### @StationEntry

Annotate `@StationEntry` on a target where you want a graph extension, and specify the scope of the dependency graph in
`parentScope` where you want to contribute the graph extension.

For example:

```kotlin
@StationEntry(parentScope = AppScope::class)
class MyActivity

// Generated FIR structure by metro-station
@GraphExtension(scope = MyActivity::class)
interface MyActivityGraphExtension {
    fun inject(target: MyActivity)

    @ContributesTo(scope = AppScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides target: MyActivity): MyActivityGraphExtension
    }
}
```

> **ℹ️ There are more features yet to come:**
> - Be able to provide params for the target (see https://github.com/bandlab/metro-station/issues/5)
> - Support custom station types (see https://github.com/bandlab/metro-station/issues/6)
> - Be able to specify target bound type, custom scope annotation, and default dependencies (see https://github.com/bandlab/metro-station/issues/4)

## Tests

The [Kotlin compiler test framework][test-framework] is set up for this project.
To create a new test, add a new `.kt` file in a [compiler-plugin/testData](compiler-plugin/testData) sub-directory:
`testData/box` for codegen tests and `testData/diagnostics` for diagnostics tests.
The generated JUnit 5 test classes will be updated automatically when tests are next run.
They can be manually updated with the `generateTests` Gradle task as well.
To aid in running tests, it is recommended to install the [Kotlin Compiler DevKit][test-plugin] IntelliJ plugin,
which is pre-configured in this repository.

[//]: # (Links)

[metro]: https://github.com/zacsweers/metro
[metro-station]: https://github.com/bandlab/metro-station/blob/main/runtime/src/commonMain/kotlin/com/bandlab/metro/station/MetroStation.kt
[station-entry]: https://github.com/bandlab/metro-station/blob/main/runtime/src/commonMain/kotlin/com/bandlab/metro/station/StationEntry.kt
[dependency-graph]: https://github.com/ZacSweers/metro/blob/main/runtime/src/commonMain/kotlin/dev/zacsweers/metro/DependencyGraph.kt
[graph-extension]: https://github.com/ZacSweers/metro/blob/main/runtime/src/commonMain/kotlin/dev/zacsweers/metro/GraphExtension.kt
[test-framework]: https://github.com/JetBrains/kotlin/blob/2.1.20/compiler/test-infrastructure/ReadMe.md
[test-plugin]: https://github.com/JetBrains/kotlin-compiler-devkit
