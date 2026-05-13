package com.bandlab.metro.station.sample.utils

import android.content.Context
import android.widget.Toast
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

interface Toaster {
    fun showToast(message: String)
}

@ContributesBinding(AppScope::class)
class ToasterImpl(private val context: Context) : Toaster {
    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}