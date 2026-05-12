package com.bandlab.uikit.compose.page.container

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bandlab.android.common.activity.CommonActivity
import com.bandlab.common.android.pager.screen.ParamPage
import com.bandlab.common.android.pager.screen.createPageInjector
import com.bandlab.uikit.api.page.Page
import com.bandlab.uikit.api.page.PageContainerState

@Suppress("UNCHECKED_CAST")
fun <T : Any> Page<T>.toPageContainerState(): PageContainerState = DefaultPageContainerState(this as Page<Any>)

@Suppress("UNCHECKED_CAST")
fun <T : Any, P : Any> ParamPage<T, P>.toPageContainerState(param: P): PageContainerState = DefaultPageContainerState(
    page = this as Page<Any>,
    param = param
)

private class DefaultPageContainerState(
    override val page: Page<Any>,
    private val param: Any? = null,
) : PageContainerState {

    /**
     * A holder for view model to survive recomposition
     */
    private var vmStore: Any? = null

    @Composable
    override fun <ViewModel : Any> Page<ViewModel>.getOrCreateViewModel(): ViewModel {
        if (vmStore == null) {
            val host = LocalActivity.current as CommonActivity<*>
            val lifecycleOwner = LocalLifecycleOwner.current

            if (param == null && this is ParamPage<*, *>) {
                error("ParamPage requires a parameter. ${this::class.java}")
            }

            val injector = page.createPageInjector(
                initialParam = param ?: Unit,
                lifecycleOwner = lifecycleOwner,
                host = host,
            )

            vmStore = injector.getPageViewModel()
        }

        @Suppress("UNCHECKED_CAST")
        return vmStore as ViewModel
    }
}