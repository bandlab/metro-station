package com.bandlab.common.android.pager.screen.di

import com.bandlab.android.common.activity.CommonActivity
import com.bandlab.uikit.api.page.PageGraphDependencies

/**
 * A set of dependencies that are required to create a page component.
 */
class AndroidPageGraphDependencies(
    val activity: CommonActivity<*>,
) : PageGraphDependencies {
    companion object {
        // A helper for JVM test to wire the AppGraph with the Page graph/ extension.
        fun fromAppGraph(appGraph: Any): AndroidPageGraphDependencies {
            val activity = object : CommonActivity<Unit>() {}
            activity.graph = appGraph
            return AndroidPageGraphDependencies(activity)
        }
    }
}