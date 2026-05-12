package com.bandlab.uikit.api.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

@Immutable
interface PageContainerState {
    val page: Page<Any>

    @Composable
    fun <ViewModel : Any> Page<ViewModel>.getOrCreateViewModel(): ViewModel

    companion object
}