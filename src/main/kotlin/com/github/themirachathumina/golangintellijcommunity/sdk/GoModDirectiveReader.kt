package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.diagnostic.Logger
import java.io.File

object GoModDirectiveReader {
    private val LOG = Logger.getInstance(GoModDirectiveReader::class.java)

    private val TOOLCHAIN_REGEX = Regex("""^toolchain\s+go([\d.]+)""", RegexOption.MULTILINE)
    private val GO_DIRECTIVE_REGEX = Regex("""^go\s+([\d.]+)""", RegexOption.MULTILINE)
    private val GO_VERSION_FILE_REGEX = """^(?:go)?(\d+\.\d+(?:\.\d+)?)""".toRegex()

    fun readGoDirective(file: File): String? {
        val content = file.readText()
        val toolchainMatch = TOOLCHAIN_REGEX.find(content)
        val goMatch = GO_DIRECTIVE_REGEX.find(content)
        return toolchainMatch?.groupValues?.get(1) ?: goMatch?.groupValues?.get(1)
    }

    fun readGoVersionFile(file: File): String? {
        if (!file.exists()) return null
        return try {
            val content = file.readText().trim()
            GO_VERSION_FILE_REGEX.find(content)?.groupValues?.get(1)
        } catch (e: Exception) {
            LOG.debug("Failed to read .go-version file at ${file.absolutePath}: ${e.message}")
            null
        }
    }
}
