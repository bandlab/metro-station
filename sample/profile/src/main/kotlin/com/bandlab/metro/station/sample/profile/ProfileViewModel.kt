// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.sample.profile

import com.bandlab.metro.station.sample.utils.ScreenTracker
import com.bandlab.metro.station.sample.utils.Toaster
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
internal class ProfileViewModel(
    private val coroutineScope: CoroutineScope,
    private val screenTracker: ScreenTracker,
    private val toaster: Toaster,
    private val profile: Profile,
) {

    fun showToast() {
        coroutineScope.launch {
            delay(1.seconds)
            screenTracker.trackScreenEnter("Profile")
            toaster.showToast("This is ${profile.name}'s profile screen")
        }
    }
}
