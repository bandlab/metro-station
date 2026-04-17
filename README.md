# metro-extensions

This project is a Kotlin compiler plugin implemented based on [Metro's extensions API][metro-fir-api].

## Use Cases

### @ContributesConfigSelector

Generates a multibinding contribution for config selectors. Annotating a class with `@ContributesConfigSelector` will
generate a nested `@ContributesTo(AppScope::class)` interface that binds the annotated class into a
`Set<DebuggableConfigSelector>` via `@Binds @IntoSet`.

```kotlin
@ContributesConfigSelector
object MyConfigSelector : BooleanConfigSelector {
    
    // This extension generates:
    @ContributesTo(AppScope::class)
    interface MultibindingContribution {
        @Binds @IntoSet
        fun bind(impl: MyConfigSelector): DebuggableConfigSelector
    }
}
```

## Project Structure

This project has three modules:

- The [`:compiler-plugin`](compiler-plugin/src) module contains the compiler plugin itself.
- The [`:plugin-annotations`](plugin-annotations/src/main/kotlin) module contains annotations which can be used in
  user code for interacting with compiler plugin.
- The [`:gradle-plugin`](gradle-plugin/src) module contains a Gradle plugin to add the compiler plugin and
  annotation dependency to a Kotlin project.

Extension point registration:

- K2 Frontend (FIR) extensions can be registered in `BandLabCompilerPluginRegistrar`.
- All other extensions (including K1 frontend and backend) can be registered in `BandLabPluginComponentRegistrar`.

## Tests

The [Kotlin compiler test framework][test-framework] is set up for this project.
To create a new test, add a new `.kt` file in a [compiler-plugin/testData](compiler-plugin/testData) sub-directory:
`testData/box` for codegen tests and `testData/diagnostics` for diagnostics tests.
The generated JUnit 5 test classes will be updated automatically when tests are next run.
They can be manually updated with the `generateTests` Gradle task as well.
To aid in running tests, it is recommended to install the [Kotlin Compiler DevKit][test-plugin] IntelliJ plugin,
which is pre-configured in this repository.

[//]: # (Links)

[metro-fir-api]: https://github.com/ZacSweers/metro/blob/main/compiler/API.md
[test-framework]: https://github.com/JetBrains/kotlin/blob/master/compiler/test-infrastructure/ReadMe.md
[test-plugin]: https://github.com/JetBrains/kotlin-compiler-devkit
