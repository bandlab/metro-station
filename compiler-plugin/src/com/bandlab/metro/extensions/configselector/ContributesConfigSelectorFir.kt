package com.bandlab.metro.extensions.configselector

import com.bandlab.metro.extensions.utils.ClassIds
import com.bandlab.metro.extensions.utils.asName
import com.fueledbycaffeine.autoservice.AutoService
import dev.zacsweers.metro.compiler.MetroOptions
import dev.zacsweers.metro.compiler.api.fir.MetroFirDeclarationGenerationExtension
import dev.zacsweers.metro.compiler.compat.CompatContext
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.FirNamedFunctionBuilder
import org.jetbrains.kotlin.fir.declarations.builder.buildNamedFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

/**
 * This FIR declaration generator generates a multibinding contribution for config selectors that are annotated with
 * [ContributesConfigSelectorIds.contributesConfigSelectorFqName].
 *
 * ```kotlin
 * @ContributesConfigSelector
 * object MyConfigSelector : BooleanConfigSelector {
 *
 *   // This extension generates:
 *   @ContributesTo(AppScope::class)
 *   interface MultibindingContribution {
 *     @Binds @IntoSet
 *     fun bind(impl: MyConfigSelector): DebuggableConfigSelector
 *   }
 * }
 * ```
 */
public class ContributesConfigSelectorFir(session: FirSession) : MetroFirDeclarationGenerationExtension(session) {

    private val predicate = ContributesConfigSelectorIds.predicate

    private val annotatedClasses by lazy {
        session.predicateBasedProvider
            .getSymbolsByPredicate(predicate)
            .filterIsInstance<FirRegularClassSymbol>()
            .toList()
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(predicate)
    }

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        // Check by classId name as a fallback, since predicate-based provider may not match
        // when multiple MetroFirDeclarationGenerationExtension instances share a composite.
        val matchesPredicate = classSymbol in annotatedClasses
        val matchesByAnnotation =
            classSymbol.resolvedCompilerAnnotationsWithClassIds.any {
                it.toAnnotationClassIdSafe(session)
                    ?.asSingleFqName() == ContributesConfigSelectorIds.contributesConfigSelectorFqName
            }
        if (matchesPredicate || matchesByAnnotation) return setOf(ContributesConfigSelectorIds.nestedContributionName)
        return emptySet()
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        if (name != ContributesConfigSelectorIds.nestedContributionName) return null
        val matchesByAnnotation =
            owner.resolvedCompilerAnnotationsWithClassIds.any {
                it.toAnnotationClassIdSafe(session)
                    ?.asSingleFqName() == ContributesConfigSelectorIds.contributesConfigSelectorFqName
            }
        if (owner !in annotatedClasses && !matchesByAnnotation) return null

        if (!owner.visibility.isPublicAPI) {
            error("${owner.classId.asString()} must be public to be contributed properly.")
        }

        val nestedClassId = owner.classId.createNestedClassId(name)
        val classSymbol = FirRegularClassSymbol(nestedClassId)

        // Build the @Binds function and add it directly to the class declarations.
        // This makes it visible to Metro's getNestedClassifiersNames (which checks for @Binds
        // functions to decide whether to generate BindsMirror).
        val bindsFunction = buildBindsFunction(nestedClassId, owner)

        val contribution = buildRegularClass {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            source = owner.source
            classKind = ClassKind.INTERFACE
            scopeProvider = session.kotlinScopeProvider
            this.name = nestedClassId.shortClassName
            symbol = classSymbol
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(owner, forClass = true),
            )
            superTypeRefs += session.builtinTypes.anyType
            val appScopeSymbol =
                session.symbolProvider.getClassLikeSymbolByClassId(ClassIds.appScope) as FirRegularClassSymbol
            annotations += buildAnnotationWithScope(
                classId = ClassIds.contributesTo,
                argName = ClassIds.scopeName,
                scopeArg = appScopeSymbol.getClassCall()
            )
            // Add the function directly to the class declarations
            declarations += bindsFunction
        }
        return contribution.symbol
    }

    private fun ClassId.firTypeRef(): FirTypeRef = buildResolvedTypeRef {
        coneType = constructClassLikeType()
    }

    /**
     * Returns a `ClassSymbol::class` expression.
     */
    private fun FirRegularClassSymbol.getClassCall(): FirExpression = buildGetClassCall {
        argumentList = buildUnaryArgumentList(
            buildResolvedQualifier {
                packageFqName = classId.packageFqName
                relativeClassFqName = classId.relativeClassName
                symbol = this@getClassCall
                resolvedToCompanionObject = false
                coneTypeOrNull = this@getClassCall.defaultType()
            }
        )
        coneTypeOrNull = StandardClassIds.KClass.constructClassLikeType(
            arrayOf(this@getClassCall.defaultType())
        )
    }

    /** Build a simple [FirAnnotation] with a scope argument. */
    internal fun buildAnnotationWithScope(
        classId: ClassId,
        argName: Name,
        scopeArg: FirExpression,
    ): FirAnnotation {
        return buildAnnotation {
            annotationTypeRef =
                ConeClassLikeTypeImpl(
                    ConeClassLikeLookupTagImpl(classId),
                    emptyArray(),
                    isMarkedNullable = false,
                )
                    .toFirResolvedTypeRef()
            argumentMapping = buildAnnotationArgumentMapping { mapping[argName] = scopeArg }
        }
    }

    private fun buildBindsFunction(
        classId: ClassId,
        outerOwner: FirClassSymbol<*>,
    ): FirDeclaration {
        val callableId = CallableId(classId, "bind".asName())

        val outerClassType = outerOwner.defaultType()
        // Build the dispatch receiver type manually since classSymbol isn't bound to FIR yet
        val dispatchType = ConeClassLikeTypeImpl(
            ConeClassLikeLookupTagImpl(classId),
            emptyArray(),
            isMarkedNullable = false,
        )

        val functionSymbol = FirNamedFunctionSymbol(callableId)

        return buildFirFunction {
            resolvePhase = FirResolvePhase.BODY_RESOLVE
            moduleData = session.moduleData
            origin = Key.origin
            symbol = functionSymbol
            name = callableId.callableName
            isLocal = false
            returnTypeRef = ContributesConfigSelectorIds.debuggableConfigSelectorClassId.firTypeRef()
            dispatchReceiverType = dispatchType
            status = FirResolvedDeclarationStatusImpl(
                Visibilities.Public,
                Modality.ABSTRACT,
                Visibilities.Public.toEffectiveVisibility(outerOwner, forClass = true),
            )
            this.valueParameters += buildValueParameter {
                resolvePhase = FirResolvePhase.BODY_RESOLVE
                moduleData = session.moduleData
                origin = Key.origin
                returnTypeRef = outerClassType.toFirResolvedTypeRef()
                this.name = "impl".asName()
                symbol = FirValueParameterSymbol()
                containingDeclarationSymbol = functionSymbol
            }
            annotations += buildSimpleAnnotationCall(ClassIds.binds, functionSymbol)
            annotations += buildSimpleAnnotationCall(ClassIds.intoSet, functionSymbol)
        }
    }

    /**
     * Build an annotation as [FirAnnotationCall] so Metro recognizes it. Metro's `metroAnnotations()`
     * checks `annotation !is FirAnnotationCall` and skips plain [FirAnnotation] instances.
     */
    @OptIn(DirectDeclarationsAccess::class)
    private fun buildSimpleAnnotationCall(
        classId: ClassId,
        containingSymbol: FirBasedSymbol<*>,
    ): FirAnnotationCall {
        val annotationType = ConeClassLikeTypeImpl(
            ConeClassLikeLookupTagImpl(classId),
            emptyArray(),
            isMarkedNullable = false,
        )
        return buildAnnotationCall {
            annotationTypeRef = annotationType.toFirResolvedTypeRef()
            argumentMapping = buildAnnotationArgumentMapping()
            calleeReference = buildResolvedNamedReference {
                name = classId.shortClassName
                resolvedSymbol =
                    session.symbolProvider.getClassLikeSymbolByClassId(classId)!!.let {
                        (it as FirClassSymbol<*>)
                            .declarationSymbols
                            .filterIsInstance<FirConstructorSymbol>()
                            .first()
                    }
            }
            containingDeclarationSymbol = containingSymbol
            annotationResolvePhase = FirAnnotationResolvePhase.Types
        }
    }

    private inline fun buildFirFunction(init: FirNamedFunctionBuilder.() -> Unit): FirNamedFunction =
        buildNamedFunction(init)

    private object Key : GeneratedDeclarationKey()

    @AutoService
    public class Factory : MetroFirDeclarationGenerationExtension.Factory {
        override fun create(
            session: FirSession,
            options: MetroOptions,
            compatContext: CompatContext,
        ): MetroFirDeclarationGenerationExtension = ContributesConfigSelectorFir(session)
    }
}