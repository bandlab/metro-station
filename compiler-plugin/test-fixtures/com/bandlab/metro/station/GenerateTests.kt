package com.bandlab.metro.station

import com.bandlab.metro.station.runners.AbstractBoxTest
import com.bandlab.metro.station.runners.AbstractDumpTest
import com.bandlab.metro.station.runners.AbstractFirDiagnosticTest
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testDataRoot = "compiler-plugin/testData", testsRoot = "compiler-plugin/test-gen") {
            testClass<AbstractFirDiagnosticTest> { model("diagnostics") }
            testClass<AbstractBoxTest> { model("box") }
            testClass<AbstractDumpTest> { model("dump") }
        }
    }
}
