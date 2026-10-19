# Stubs

This module contains stubs for Android APIs and internal BandLab Android code. These stubs provide the minimal type signatures
and test helpers needed by the compiler tests without pulling in Android or the actual internal dependencies. It is a JVM module
so compiler tests can run without an Android SDK. See the [Android sample](../sample/README.md) for a runnable example.

The implementations are deliberately simplified: for example, `PageParamFlowProvider` emits only the initial parameter,
and `CommonActivity.setAppGraphAndInject` supplies a graph directly for tests. These stubs do not model Android lifecycle behavior.

This module is not published and exists solely to support development of the compiler plugin.
