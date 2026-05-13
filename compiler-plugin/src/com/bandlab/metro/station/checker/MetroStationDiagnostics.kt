package com.bandlab.metro.station.checker

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.NAME_IDENTIFIER
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers.STRING
import org.jetbrains.kotlin.psi.KtElement

internal object MetroStationDiagnostics : KtDiagnosticsContainer() {

    val RESTRICTED_PARAM_TYPE by error1<KtElement, String>(NAME_IDENTIFIER)
    val DEPRECATED_CONTRIBUTES_INJECTOR by error0<KtElement>(NAME_IDENTIFIER)
    val TARGET_MUST_BE_PUBLIC by error1<KtElement, String>(NAME_IDENTIFIER)
    val MISSING_CONTEXT_PARAMETER by error1<KtElement, String>(NAME_IDENTIFIER)
    val MISSING_EXTRA_DEPENDENCIES_PARAMETER by error1<KtElement, String>(NAME_IDENTIFIER)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = RendererFactory

    private object RendererFactory : BaseDiagnosticRendererFactory() {
        override val MAP by KtDiagnosticFactoryToRendererMap("ParamTypeErrors") { map ->
            map.put(RESTRICTED_PARAM_TYPE, "{0}", STRING)
            map.put(
                DEPRECATED_CONTRIBUTES_INJECTOR,
                "@StationEntry is deprecated. Use @MetroStation instead.",
            )
            map.put(TARGET_MUST_BE_PUBLIC, "{0}", STRING)
            map.put(MISSING_CONTEXT_PARAMETER, "{0}", STRING)
            map.put(MISSING_EXTRA_DEPENDENCIES_PARAMETER, "{0}", STRING)
        }
    }
}