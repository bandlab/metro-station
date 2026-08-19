package com.bandlab.metro.station.sample.profile.content

import com.bandlab.metro.station.sample.profile.ProfileService
import com.bandlab.metro.station.sample.utils.ScreenTracker
import dev.zacsweers.metro.Inject
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Inject
class ProfileContentViewModel(
    private val profileService: ProfileService,
    private val coroutineScope: CoroutineScope,
    private val screenTimer: Flow<Duration>,
    private val additionalFeature: AdditionalFeature,
    screenTracker: ScreenTracker,
) {
    val username: StateFlow<String?>
        field = MutableStateFlow(null)

    val description: StateFlow<String>
        field = MutableStateFlow("")

    init {
        screenTracker.trackScreenEnter("ProfileContent")

        coroutineScope.launch {
            screenTimer.collect { duration ->
                description.value =
                    "Screen has been open for ${duration.inWholeSeconds} seconds." +
                        "\n" +
                        "Feature: ${additionalFeature.featureName}"
            }
        }
    }

    fun loadUser() {
        coroutineScope.launch {
            username.value = profileService.getUsername()
        }
    }
}