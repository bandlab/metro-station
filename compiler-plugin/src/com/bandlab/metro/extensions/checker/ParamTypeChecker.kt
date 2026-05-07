package com.bandlab.metro.extensions.checker

import com.bandlab.metro.extensions.utils.findSuperTypeRef
import com.bandlab.metro.extensions.utils.typeArgumentSource
import com.bandlab.metro.extensions.utils.unwrapType
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.StandardClassIds
import com.bandlab.metro.extensions.component.ContributesComponentIds as Ids

internal object ParamTypeClassChecker : FirDeclarationChecker<FirClass>(MppCheckerKind.Common) {

    private val restrictedParamTypes = setOf(
        StandardClassIds.String,
        StandardClassIds.Int,
        StandardClassIds.Long,
        StandardClassIds.Boolean,
        StandardClassIds.Float,
        StandardClassIds.Double,
        StandardClassIds.Char,
        StandardClassIds.Byte,
        StandardClassIds.Short,
    )

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val symbol = declaration.symbol
        val session = context.session

        // Only check classes annotated with @ContributesComponent or @ContributesInjector
        val hasComponent = symbol.getAnnotationByClassId(Ids.contributesComponent, session) != null
        val hasInjector = symbol.getAnnotationByClassId(Ids.contributesInjector, session) != null
        if (!hasComponent && !hasInjector) return

        // Check CommonActivity<Param> - param is the first type arg
        val activitySuperTypeRef = symbol.findSuperTypeRef(Ids.commonActivity)
        if (activitySuperTypeRef != null) {
            val paramType = activitySuperTypeRef.unwrapType(0) as? ConeKotlinType
            if (paramType != null && paramType.classId != StandardClassIds.Unit) {
                val source = activitySuperTypeRef.typeArgumentSource(0)
                checkRestricted(paramType, source, declaration)
            }
            return
        }

        // Check ParamPage<ViewModel, Param> - param is the second type arg
        val paramPageSuperTypeRef = symbol.findSuperTypeRef(Ids.paramPage)
        if (paramPageSuperTypeRef != null) {
            val paramType = paramPageSuperTypeRef.unwrapType(1) as? ConeKotlinType
            if (paramType != null) {
                val source = paramPageSuperTypeRef.typeArgumentSource(1)
                checkRestricted(paramType, source, declaration)
            }
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkRestricted(paramType: ConeKotlinType, typeArgSource: KtSourceElement?, declaration: FirClass) {
        if (paramType.classId in restrictedParamTypes) {
            reporter.reportOn(
                source = typeArgSource ?: declaration.source,
                factory = MetroExtensionsDiagnostics.RESTRICTED_PARAM_TYPE,
                context = context
            )
        }
    }
}
