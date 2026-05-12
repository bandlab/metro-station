package com.bandlab.uikit.api.page

import androidx.compose.runtime.Composable

interface Page<ViewModel : Any> {

    @Composable
    fun Content(viewModel: ViewModel)
}