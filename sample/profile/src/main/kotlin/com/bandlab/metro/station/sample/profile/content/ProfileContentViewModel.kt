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
    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    init {
        screenTracker.trackScreenEnter("ProfileContent")
    }

    fun loadUser() {
        coroutineScope.launch {
            _username.value = profileService.getUsername()
        }
    }
}