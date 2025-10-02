package com.bandlab.metro.station

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.NAME_IDENTIFIER
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

internal object MetroStationDiagnostics : KtDiagnosticsContainer() {

    val STATION_ENTRY_NOT_ON_CLASS by error0(NAME_IDENTIFIER)

    override fun getRendererFactory(): BaseDiagnosticRendererFactory {
        return FirMetroStationErrorMessages
    }
}

private object FirMetroStationErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP by KtDiagnosticFactoryToRendererMap("MetroStation") { map ->
        map.apply {
            put(
                MetroStationDiagnostics.STATION_ENTRY_NOT_ON_CLASS,
                "@StationEntry can only be applied to classes"
            )
        }
    }
}