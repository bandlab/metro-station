// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.sample.profile.footer

import com.bandlab.metro.station.sample.utils.ScreenTracker
import dev.zacsweers.metro.Inject

@Inject
class ProfileFooterViewModel(
    screenTracker: ScreenTracker,
    param: ProfileFooterPage.Param,
) {
    val username = param.username

    init {
        screenTracker.trackScreenEnter("ProfileFooter")
    }
}
