package com.bandlab.metro.extensions.checker

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtElement

internal object MetroExtensionsDiagnostics : KtDiagnosticsContainer() {

    val RESTRICTED_PARAM_TYPE by error0<KtElement>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = RendererFactory

    private object RendererFactory : BaseDiagnosticRendererFactory() {
        override val MAP by KtDiagnosticFactoryToRendererMap("ParamTypeErrors") { map ->
            map.put(
                RESTRICTED_PARAM_TYPE,
                "Parameter type is a restricted primitive type. Use a wrapper class instead.",
            )
        }
    }
}