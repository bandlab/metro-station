package com.bandlab.uikit.compose.page.container

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bandlab.uikit.api.page.PageContainerState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PageContainer(state: PageContainerState) {
    val page = state.page
    val vm = with(state) { page.getOrCreateViewModel() }
    page.Content(vm)
}

@Composable
fun PageContainer(state: StateFlow<PageContainerState?>) {
    val currentState = state.collectAsStateWithLifecycle().value
    if (currentState != null) {
        PageContainer(state = currentState)
    }
}