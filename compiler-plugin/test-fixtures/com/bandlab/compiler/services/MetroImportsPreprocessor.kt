package com.bandlab.metro.extensions.services

import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.ReversibleSourceFilePreprocessor
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.isJavaFile

/**
 * Preprocessor that automatically adds Metro imports to test files. This allows test files to use
 * Metro annotations without explicit imports.
 */
class MetroImportsPreprocessor(testServices: TestServices) :
    ReversibleSourceFilePreprocessor(testServices) {

    private val additionalImports: Set<String> = setOf("dev.zacsweers.metro.*")

    private val additionalImportsString: String by lazy {
        additionalImports.sorted().joinToString(separator = "\n") { "import $it" }
    }

    override fun process(file: TestFile, content: String): String {
        if (file.isAdditional) return content
        if (file.isJavaFile) return content

        val lines = content.lines().toMutableList()
        when (val packageIndex = lines.indexOfFirst { it.startsWith("package ") }) {
            // No package declaration found.
            -1 ->
                when (val nonBlankIndex = lines.indexOfFirst { it.isNotBlank() }) {
                    // No non-blank lines? Place imports at the very beginning...
                    -1 -> lines.add(0, additionalImportsString)
                    // Place imports before first non-blank line.
                    else -> lines.add(nonBlankIndex, additionalImportsString)
                }
            // Place imports just after package declaration.
            else -> lines.add(packageIndex + 1, additionalImportsString)
        }
        return lines.joinToString(separator = "\n")
    }

    override fun revert(file: TestFile, actualContent: String): String {
        if (file.isAdditional) return actualContent
        if (file.isJavaFile) return actualContent
        return actualContent.replace(additionalImportsString + "\n", "")
    }
}
