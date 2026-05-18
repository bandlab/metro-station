package com.bandlab.metro.station.sample.utils

import android.content.Context
import android.widget.Toast
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

interface Toaster {
    fun showToast(message: String)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ToasterImpl(private val context: Context) : Toaster {
    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}