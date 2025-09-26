package com.bandlab.metro.station.runners

import org.jetbrains.kotlin.test.services.TestServices

class MetroStationDefaultImportPreprocessor(testServices: TestServices) : ImportsPreprocessor(testServices) {
    override val additionalImports: Set<String> = setOf("com.bandlab.metro.station.*")
}
