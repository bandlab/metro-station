# Metro Station Sample

This is a sample Android app demonstrating the usage of the `metro-station` Kotlin Compiler Plugin.

## Overview

The sample app illustrates how to streamline dependency injection and eliminate KSP usage in an Android app using Metro's extensions API. It showcases the plugin's capability to generate dependency graphs and extensions automatically.

## Key Features Demonstrated

- **`@MetroStation` Usage**: Generates a standalone dependency graph for a feature. Start with [`MainActivity`](app/src/main/kotlin/com/bandlab/metro/station/sample/main/MainActivity.kt), which requests app-level dependencies through a service-provider contract.
- **Page Dependencies**: [`ProfileContentPage`](profile/src/main/kotlin/com/bandlab/metro/station/sample/profile/content/ProfileContentPage.kt) combines app dependencies, activity-provided extra dependencies, an additional scope, exclusions, and an explicit binding container. [`ProfileFooterPage`](profile/src/main/kotlin/com/bandlab/metro/station/sample/profile/footer/ProfileFooterPage.kt) demonstrates a typed page parameter.
- **`@StationEntry` Usage**: [`ProfileActivity`](profile/src/main/kotlin/com/bandlab/metro/station/sample/profile/ProfileActivity.kt) demonstrates the legacy graph-extension approach. This annotation is in maintenance mode; prefer `@MetroStation` for new features.
- **Android Component Injection**: [`SampleApp`](app/src/main/kotlin/com/bandlab/metro/station/sample/SampleApp.kt) starts services, sends a broadcast, and enqueues a worker to exercise their generated injection hooks.
- **Config Selector Contributions**: [`ProfileBannerConfigSelector`](profile/src/main/kotlin/com/bandlab/metro/station/sample/profile/ProfileBannerConfigSelector.kt) contributes to the app graph's config-selector set.
- **Compiler Plugin Ordering**: Metro Station must run **before Metro during the IR phase**, so Metro can process generated expressions such as `createGraphFactory`. [`SetupMetroPlugin`](sample-build-logic/src/main/kotlin/SetupMetroPlugin.kt) configures `-Xcompiler-plugin-order=com.bandlab.metro.station>dev.zacsweers.metro.compiler` for each Kotlin compilation task.

The `utils` module contains simplified versions of BandLab's internal framework contracts. In particular, `PageParamFlowProvider` emits only the initial parameter; this sample does not implement parameter updates from `onNewIntent` or saved-state restoration.

## Getting Started

The sample is a separate Gradle build that includes the repository root as a composite build, so local compiler-plugin changes are used automatically without publishing artifacts.

1. Use JDK 25 and install Android SDK Platform 37 (see the [sample version catalog](gradle/libs.versions.toml)).
2. Open the **`sample/` directory** in Android Studio and let Gradle sync. Configure the SDK location through Android Studio, `sample/local.properties` (`sdk.dir=...`), or `ANDROID_HOME`.
3. Run the `app` configuration on an emulator or device running Android 7.0 (API 24) or newer.

To assemble from the repository root:

```bash
./gradlew -p sample :app:assembleDebug
```

Launch the app and tap **Open Profile** to see both page examples. The Android components run at application startup; search Logcat for `MetroStation::` to see their log messages.

The `app` module's `ExampleUnitTest` and `ExampleInstrumentedTest` are Android template smoke tests. Compiler-plugin behavior is covered by the repository's [compiler tests](../compiler-plugin/testData/README.md).
