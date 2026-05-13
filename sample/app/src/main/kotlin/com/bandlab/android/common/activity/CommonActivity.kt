package com.bandlab.android.common.activity

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.bandlab.common.android.di.AndroidInjection
import com.bandlab.metro.station.sample.utils.ScreenTracker

abstract class CommonActivity<Params : Any> : FragmentActivity() {

    lateinit var params: Params
        private set

    abstract val dependencies: CommonActivityDependencies

    protected abstract fun parseRequiredParams(bundle: Bundle): Params

    protected abstract fun onCreate()

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            params = parseRequiredParams(intent.extras ?: Bundle.EMPTY)
        } catch (_: Exception) {
            Toast.makeText(this, "Required param is missing.", Toast.LENGTH_SHORT).show()
            finish()
            super.onCreate(savedInstanceState)
            return
        }

        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        onCreate()
    }

    interface ServiceProvider {
        val screenTracker: ScreenTracker
    }
}
