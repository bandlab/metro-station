// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

// Hack to support version catalog
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
inline val Project.libs
    get() = extensions.getByType<org.gradle.accessors.dm.LibrariesForLibs>()
