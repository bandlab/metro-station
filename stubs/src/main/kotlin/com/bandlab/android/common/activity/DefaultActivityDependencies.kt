package com.bandlab.android.common.activity

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides

@BindingContainer
object DefaultActivityDependencies {

    @Provides
    fun provideType(activity: CommonActivity<*>): String = activity.type
}