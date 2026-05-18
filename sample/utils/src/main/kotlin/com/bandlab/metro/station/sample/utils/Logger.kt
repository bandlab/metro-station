package com.bandlab.metro.station.sample.utils

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn

interface Logger {
    fun log(message: String)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class LoggerImpl(private val context: Context) : Logger {
    override fun log(message: String) {
        java.util.logging.Logger.getLogger(context.applicationInfo.name)
            .info(message)
    }
}