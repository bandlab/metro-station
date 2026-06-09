package com.bandlab.metro.station.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import com.bandlab.metro.station.graph.MetroStationIds as Ids

/**
 * Generates either:
 * - `return feature.params` (for CommonActivity)
 * - `return pageGraphDependencies.initialParam as ParamType` (for ParamPage)
 */
internal fun generateProvideParamBody(pluginContext: IrPluginContext, declaration: IrSimpleFunction) {
    val regularParams = declaration.parameters.filter { it.kind == IrParameterKind.Regular }
    val firstParam = regularParams.first()
    val firstParamClass = firstParam.type.classOrNull?.owner ?: return

    // Check if the parameter is PageGraphDependencies (ParamPage case)
    val initialParamProperty = firstParamClass.declarations
        .filterIsInstance<IrProperty>()
        .find { it.name == Ids.initialParamName }
    if (initialParamProperty != null) {
        val initialParamGetter = initialParamProperty.getter!!.symbol
        // ParamPage case: return pageGraphDependencies.initialParam as ParamType
        val returnType = declaration.returnType
        declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
            val getInitialParam = irCall(initialParamGetter).apply {
                dispatchReceiver = irGet(firstParam)
            }
            +irReturn(
                IrTypeOperatorCallImpl(
                    startOffset, endOffset,
                    returnType,
                    IrTypeOperator.CAST,
                    returnType,
                    getInitialParam
                )
            )
        }
    } else {
        // CommonActivity case: return feature.params
        val paramsGetterSymbol = firstParamClass.getPropertyGetter("params") ?: return
        declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
            +irReturn(
                irCall(paramsGetterSymbol).apply {
                    dispatchReceiver = irGet(firstParam)
                }
            )
        }
    }
}

/**
 * Generates: `return feature`
 */
internal fun generateProvideBaseTypeBody(pluginContext: IrPluginContext, declaration: IrSimpleFunction) {
    val featureParameter = declaration.parameters.first { it.kind == IrParameterKind.Regular }

    declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
        +irReturn(irGet(featureParameter))
    }
}

/**
 * Generates: `return provider.createParamFlow(feature, initialParam)`
 */
internal fun generateProvideParamFlowBody(pluginContext: IrPluginContext, declaration: IrSimpleFunction) {
    val regularParams = declaration.parameters.filter { it.kind == IrParameterKind.Regular }
    val providerParam = regularParams[0]
    val featureParam = regularParams[1]
    val initialParamParam = regularParams[2]

    val providerClass = providerParam.type.classOrNull?.owner ?: return
    val createParamFlowFunction = providerClass.functions.firstOrNull { it.name.asString() == "createParamFlow" }
        ?: return

    declaration.body = DeclarationIrBuilder(pluginContext, declaration.symbol).irBlockBody {
        +irReturn(
            irCall(createParamFlowFunction).apply {
                // arguments[0] = dispatch receiver
                arguments[0] = irGet(providerParam)
                // arguments[1] = page (first value param)
                arguments[1] = irGet(featureParam)
                // arguments[2] = initialParam (second value param)
                arguments[2] = irGet(initialParamParam)
            }
        )
    }
}
