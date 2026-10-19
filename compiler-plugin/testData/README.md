# Compiler Test Data

Run these tests from the repository root with `./gradlew :compiler-plugin:test`. The runners in `../test-fixtures` register both Metro Station and Metro, with Metro Station's IR extensions running first.

## Test Suites

| Directory | What it verifies | Expected result |
| --- | --- | --- |
| `box/` | Compiles and executes generated code on the JVM | `fun box(): String` completes its assertions and returns `"OK"` |
| `diagnostics/` | Reports errors for unsupported declarations or project-policy violations | Inline `<!DIAGNOSTIC_NAME!>...<!>` markers match the reported diagnostics |
| `dump/` | Checks generated declarations and function bodies | Adjacent `.fir.txt` (FIR) and `.kt.txt` (Kotlin-like IR) golden files match the compiler output |

Within `box/` and `dump/`, `station` covers `@MetroStation` dependency graphs, `entry` covers legacy `@StationEntry` graph extensions, and `configselector` covers `@ContributesConfigSelector` set contributions.

## Reading Fixtures

- Common Metro, Metro Station, and BandLab framework imports are added by `configureImports` in `ExtensionRegistrarConfigurator.kt`. Box tests also receive `kotlin.test.*`; the omitted imports are intentional.
- Framework types come from the JVM-only [`stubs`](../../stubs/README.md) module. `setAppGraphAndInject` installs a graph on the activity stub and invokes the generated injection method. `AndroidPageGraphDependencies.fromAppGraph` supplies a stub host for page injection.
- `// MODULE: lib` and `// MODULE: main(lib)` split one fixture into separately compiled modules. These cases check that generated contributions or scope markers remain usable across module boundaries.
- Directives such as `// DISABLE_STATION_ENTRIES` and `// ENABLE_ACTIVITY_BASELINE: ...` configure project-specific migration policies. They are not general restrictions imposed by Metro.
- Parameter-flow box tests check the initial parameter and the first flow emission. The stub flow does not simulate later `onNewIntent` updates.
- The Android component box tests in `box/station/other` explicitly create a graph and inject members. Generated lifecycle callback injection is checked by `dump/station/other` and exercised by the Android sample.

## Adding or Updating a Case

Keep each fixture focused on a behavior. Use a descriptive filename and add a short comment when the setup or regression is not obvious, especially for combined scopes, cross-module inputs, or simplified stub behavior.

Add or edit `.kt` fixtures rather than the generated Java runners in `../test-gen`. Runners are regenerated when tests run, or explicitly with `./gradlew :compiler-plugin:generateTests`. Review golden-file diffs when changing generated output. Diagnostic markers and intentionally invalid Kotlin are part of the test format, so this directory is excluded from automatic formatting.
