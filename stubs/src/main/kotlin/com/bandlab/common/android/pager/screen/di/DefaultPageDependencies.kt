package com.bandlab.common.android.pager.screen.di

import com.bandlab.uikit.api.page.Page
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides

/**
 * Provides default dependencies for a Page.
 */
@BindingContainer
object DefaultPageDependencies {

    @Provides
    fun provideType(page: Page<*>): String = page.type
}