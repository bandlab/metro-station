package com.bandlab.metro.extensions.services

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object MetroDirectives : SimpleDirectivesContainer() {
    val GENERATE_CONTRIBUTION_HINTS_IN_FIR by
    directive("Enable generation of contribution hints in FIR instead of IR.")
}
