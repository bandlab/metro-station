package com.bandlab.metro.station.sample.profile.content

import com.bandlab.metro.station.sample.profile.ProfileService
import com.bandlab.metro.station.sample.utils.ScreenTracker
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Inject
class ProfileContentViewModel(
    private val profileService: ProfileService,
    private val coroutineScope: CoroutineScope,
    screenTracker: ScreenTracker,
) {
    val username: StateFlow<String?>
        field = MutableStateFlow(null)

    init {
        screenTracker.trackScreenEnter("ProfileContent")
    }

    fun loadUser() {
        coroutineScope.launch {
            username.value = profileService.getUsername()
        }
    }
}