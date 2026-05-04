package com.bandlab.uikit.api.page

import androidx.compose.runtime.Composable

interface Page<ViewModel : Any> {

    val type: String
        get() = "Page"

    @Composable
    fun Content(viewModel: ViewModel) = Unit
}