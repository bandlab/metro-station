package com.bandlab.metro.station.sample.main

import android.content.Intent
import androidx.activity.ComponentActivity
import com.bandlab.metro.station.sample.profile.Profile
import com.bandlab.metro.station.sample.profile.ProfileActivity
import com.bandlab.metro.station.sample.utils.ScreenTracker
import com.bandlab.metro.station.sample.utils.Toaster
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@Inject
internal class MainViewModel(
    private val coroutineScope: CoroutineScope,
    private val screenTracker: ScreenTracker,
    private val toaster: Toaster,
    private val componentActivity: ComponentActivity
) {

    fun showToast() {
        coroutineScope.launch {
            delay(1.seconds)
            screenTracker.trackScreenEnter("Main")
            toaster.showToast("This is the main screen")
        }
    }

    fun openProfile() {
        val intent = Intent(componentActivity, ProfileActivity::class.java)
        intent.putExtra(
            ProfileActivity.ARG_PROFILE,
            Json.encodeToString(Profile(id = "001", name = "BandLab"))
        )
        componentActivity.startActivity(intent)
    }
}