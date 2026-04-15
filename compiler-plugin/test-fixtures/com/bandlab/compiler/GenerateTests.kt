package com.bandlab.compiler

import com.bandlab.compiler.runners.AbstractBoxTest
import com.bandlab.compiler.runners.AbstractFirDiagnosticTest
import com.bandlab.compiler.runners.AbstractFirDumpTest
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testDataRoot = "compiler-plugin/testData", testsRoot = "compiler-plugin/test-gen") {
            testClass<AbstractFirDiagnosticTest> { model("diagnostics") }
            testClass<AbstractBoxTest> { model("box") }
            testClass<AbstractFirDumpTest> { model("dump") }
        }
    }
}
