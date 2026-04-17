// GENERATE_CONTRIBUTION_HINTS_IN_FIR
package com.test

import com.bandlab.config.api.ContributesConfigSelector
import com.bandlab.config.api.DebuggableConfigSelector

// MODULE: lib
@ContributesConfigSelector
object FooConfigSelector : DebuggableConfigSelector

// MODULE: main(lib)
package com.test

fun box(): String {
    // Verify that the scope hint function was generated for the @ContributesTo interface.
    // Metro uses these hints to discover cross-module contributions.
    val hintClass = try {
        Class.forName("metro.hints.ComTestFooConfigSelectorMultibindingContributionAppScopeKt")
    } catch (e: ClassNotFoundException) {
        return "FAIL: Scope hint not generated for FooConfigSelector.MultibindingContribution"
    }

    val hintFunction = hintClass.methods.find { it.name == "AppScope" }
        ?: return "FAIL: Hint function 'AppScope' not found in ${hintClass.name}"

    return "OK"
}
