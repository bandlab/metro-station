# Metro Station Sample

This is a sample Android app demonstrating the usage of the `metro-station` Kotlin Compiler Plugin.

## Overview

The sample app illustrates how to streamline dependency injection and eliminate KSP usage in an Android app using Metro's extensions API. It showcases the plugin's capability to generate dependency graphs and extensions automatically.

## Key Features Demonstrated

- **`@MetroStation` Usage**: Generates a standalone dependency graph for a feature. See `MainActivity` and `ProfileContentPage`, it automatically wires up dependencies from the `AppGraph`.
- **`@StationEntry` Usage**: Demonstrates contributing a graph extension towards a declared parent scope, as seen in `ProfileActivity`.
- **Run metro-station after metro**: Because we generate metro expressions like `createGraphFactory` that require metro compiler to run after ours during the IR phase, we need to leverate the `-Xcompiler-plugin-order` flag to ensure that metro-station runs after metro.

## Getting Started

To run the sample:
1. Open the project in Android Studio.
2. Build and run the `:sample:app` configuration on an emulator or physical device.