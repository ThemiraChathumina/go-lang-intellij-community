package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.diagnostic.Logger
import java.io.File

object GoModDirectiveReader {
    private val LOG = Logger.getInstance(GoModDirectiveReader::class.java)

    fun readGoDirective(file: File): String? {
        val content = file.readText()
        val toolchainMatch = Regex("""^toolchain\s+go([\d.]+)""", RegexOption.MULTILINE).find(content)
        val goMatch = Regex("""^go\s+([\d.]+)""", RegexOption.MULTILINE).find(content)
        return toolchainMatch?.groupValues?.get(1) ?: goMatch?.groupValues?.get(1)
    }

    fun readGoVersionFile(file: File): String? {
        if (!file.exists()) return null
        return try {
            val content = file.readText().trim()
            val regex = """^(?:go)?(\d+\.\d+(?:\.\d+)?)""".toRegex()
            regex.find(content)?.groupValues?.get(1)
        } catch (e: Exception) {
            LOG.debug("Failed to read .go-version file at ${file.absolutePath}: ${e.message}")
            null
        }
    }
}
