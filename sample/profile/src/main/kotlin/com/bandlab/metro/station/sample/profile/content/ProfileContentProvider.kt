// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.sample.profile.content

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@BindingContainer
object ProfileContentProvider {

    @Provides
    fun provideTickerFlow(): Flow<Duration> = flow {
        val startTime = TimeSource.Monotonic.markNow()
        while (true) {
            emit(startTime.elapsedNow())
            delay(100.milliseconds)
        }
    }
}

@ContributesTo(ProfileContentPage::class)
@BindingContainer
object FakeProfileContentProvider {

    @Provides fun provideTickerFlow(): Flow<Duration> = flowOf(Duration.ZERO)
}

interface AdditionalFeatureScope

class AdditionalFeature(val featureName: String)

@ContributesTo(AdditionalFeatureScope::class)
@BindingContainer
object AdditionalFeatureProvider {

    @Provides
    fun provideAdditionalFeature(): AdditionalFeature = AdditionalFeature("Sample Feature")
}
