package com.bandlab.metro.extensions

import com.bandlab.metro.extensions.checker.MetroExtensionsFirCheckers
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

public class BandLabCompilerPluginRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::MetroExtensionsFirCheckers
    }
}
