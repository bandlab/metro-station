// Copyright 2026 BandLab Singapore Pte Ltd
// SPDX-License-Identifier: Apache-2.0
package com.bandlab.metro.station.sample.profile

import com.bandlab.config.api.BooleanConfigSelector
import com.bandlab.metro.station.ContributesConfigSelector

@ContributesConfigSelector
object ProfileBannerConfigSelector : BooleanConfigSelector {
    override val key: String
        get() = "profile_banner_enabled"

    override val defaultValue: Boolean
        get() = false
}
