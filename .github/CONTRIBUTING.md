# Contributing

## Tests

The [Kotlin compiler test framework][test-framework] is set up for this project.
To create a new test, add a new `.kt` file in a [compiler-plugin/testData](compiler-plugin/testData) sub-directory:
`testData/box` for codegen tests and `testData/diagnostics` for diagnostics tests.
The generated JUnit 5 test classes will be updated automatically when tests are next run.
They can be manually updated with the `generateTests` Gradle task as well.
To aid in running tests, it is recommended to install the [Kotlin Compiler DevKit][test-plugin] IntelliJ plugin,
which is pre-configured in this repository.

## Code style

This repository uses [Kempt](https://github.com/ZacSweers/kempt) to enforce code
style. Kempt runs [ktfmt](https://github.com/facebook/ktfmt) (`kotlinlang` style)
on Kotlin sources, sorts Gradle dependency blocks, normalizes trailing
whitespace, and inserts Apache 2.0 license headers. Configuration lives in
[`.kempt.toml`](../.kempt.toml).

CI runs `kempt check` on every push and pull request via the `format-check` job
in [`.github/workflows/build.yml`](workflows/build.yml). A pull request will fail
this check if any file is not formatted.

### Install Kempt

A working Git 2.25+ and a JDK 17+ on your `PATH` are required.

```bash
# Homebrew (macOS, Linux)
brew install ZacSweers/tap/kempt-fmt

# or the shell installer
curl --proto '=https' --tlsv1.2 -LsSf \
  https://github.com/ZacSweers/kempt/releases/latest/download/kempt-fmt-installer.sh | sh

# or via Cargo
cargo install kempt-fmt
```

### Set up the commit hook locally

Install the pre-commit hook so your staged files are formatted automatically
before each commit:

```bash
kempt install-hook
```

This writes `.git/hooks/pre-commit`, which calls `kempt hook`. In the default
`format` mode the hook formats matching staged files and re-stages only the
files it changed before the commit continues.

Because `.git/hooks/` is local Git metadata, every contributor must run
`kempt install-hook` once after cloning.

### Formatting manually

```bash
kempt format        # format all tracked files in place
kempt format --staged   # format only staged files
kempt check         # read-only; exits non-zero if anything needs formatting (what CI runs)
```

[//]: # (Links)

[test-framework]: https://github.com/JetBrains/kotlin/blob/master/compiler/test-infrastructure/ReadMe.md
[test-plugin]: https://github.com/JetBrains/kotlin-compiler-devkit
