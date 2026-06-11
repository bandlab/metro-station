package com.bandlab.android.common.activity

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.bandlab.common.di.GeneratedByMetroStation
import com.bandlab.metro.station.sample.utils.ScreenTracker

/**
 * An abstraction for activities that require dependency injection and parameter parsing.
 *
 * @param Params The type of the parameters required by the activity.
 */
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

        @OptIn(GeneratedByMetroStation::class)
        inject()
        super.onCreate(savedInstanceState)
        onCreate()
    }

    /**
     * Creates the dependency graph (or graph extension), and performs members injection for the Activity.
     */
    @GeneratedByMetroStation
    open fun inject() {
        throw UnsupportedOperationException("This method will be implemented by the compiler plugin.")
    }

    // Used by the compiler plugin
    interface ServiceProvider {
        val screenTracker: ScreenTracker
    }
}
