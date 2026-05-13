package com.bandlab.android.common.activity

import com.bandlab.metro.station.sample.utils.ScreenTracker
import dev.zacsweers.metro.Inject

@Inject
class CommonActivityDependencies(
    val screenTracker: ScreenTracker,
)