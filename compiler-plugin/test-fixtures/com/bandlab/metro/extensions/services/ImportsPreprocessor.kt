package com.bandlab.metro.extensions.services

import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.ReversibleSourceFilePreprocessor
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.isJavaFile

/**
 * Preprocessor that automatically adds a set of imports to test files.
 */
class ImportsPreprocessor(testServices: TestServices, imports: Set<String>) :
    ReversibleSourceFilePreprocessor(testServices) {

    private val importsString: String by lazy {
        imports.sorted().joinToString(separator = "\n") { "import $it" }
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
                    -1 -> lines.add(0, importsString)
                    // Place imports before first non-blank line.
                    else -> lines.add(nonBlankIndex, importsString)
                }
            // Place imports just after package declaration.
            else -> lines.add(packageIndex + 1, importsString)
        }
        return lines.joinToString(separator = "\n")
    }

    override fun revert(file: TestFile, actualContent: String): String {
        if (file.isAdditional) return actualContent
        if (file.isJavaFile) return actualContent
        return actualContent.replace(importsString + "\n", "")
    }
}
