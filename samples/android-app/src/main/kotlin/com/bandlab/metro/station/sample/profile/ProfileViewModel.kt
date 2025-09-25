package com.bandlab.metro.station.sample.profile

import android.content.Context
import android.widget.Toast
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Inject
internal class ProfileViewModel(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    fun showToast() {
        coroutineScope.launch {
            delay(1.seconds)
            Toast.makeText(context, "This is profile screen", Toast.LENGTH_SHORT).show()
        }
    }
}