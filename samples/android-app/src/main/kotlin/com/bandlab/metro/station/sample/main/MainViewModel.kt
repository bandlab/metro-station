package com.bandlab.metro.station.sample.main

import android.content.Context
import android.widget.Toast
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Inject
internal class MainViewModel(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    fun showToast() {
        coroutineScope.launch {
            delay(1.seconds)
            Toast.makeText(context, "This is main screen", Toast.LENGTH_SHORT).show()
        }
    }
}