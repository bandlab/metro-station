// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.common.android.di

import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides

/** Base factory interface for building standalone dependency graphs. */
interface GraphFactory<Feature, ServiceProvider, ExtraDependencies, Graph> {
    fun create(
        @Provides feature: Feature,
        @Includes serviceProvider: ServiceProvider,
        @Includes extraDependencies: ExtraDependencies,
    ): Graph
}

object EmptyExtraDependencies
