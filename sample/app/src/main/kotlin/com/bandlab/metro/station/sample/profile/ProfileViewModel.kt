package com.bandlab.metro.station.sample.profile

import com.bandlab.metro.station.sample.utils.ScreenTracker
import com.bandlab.metro.station.sample.utils.Toaster
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

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