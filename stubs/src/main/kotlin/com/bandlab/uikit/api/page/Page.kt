package com.bandlab.uikit.api.page

import androidx.compose.runtime.Composable
import com.bandlab.common.android.pager.screen.di.PageGraphDependencies
import com.bandlab.common.di.GeneratedByMetroStation

interface Page<ViewModel : Any> {

    val type: String
        get() = "Page"

    @Composable
    fun Content(viewModel: ViewModel) = Unit

    @GeneratedByMetroStation
    fun injectViewModel(deps: PageGraphDependencies): ViewModel {
        throw UnsupportedOperationException("This method will be implemented by the compiler plugin.")
    }
}