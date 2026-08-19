// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.sample.profile

import com.bandlab.metro.station.sample.utils.AnalyticsTracker
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@SingleIn(ProfileActivity::class)
@Inject
class ProfileService(
    private val tracker: AnalyticsTracker,
    private val profile: Profile,
) {
    suspend fun getUsername(): String {
        delay(1.seconds)
        tracker.trackEvent("profile_username_retrieved")
        return profile.name
    }
}
