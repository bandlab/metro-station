package com.bandlab.android.common

import com.bandlab.metro.station.sample.utils.Toaster

/**
 * Default app-level dependencies that are widely used in screens.
 */
interface DefaultScreenServiceProvider {
    val toaster: Toaster
}