package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.util.FileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.RealFileSystemAccess
import com.intellij.openapi.diagnostic.Logger
import java.io.File

/*
 * Reads and parses Go version directives from 'go.mod', 'go.work', and '.go-version' files.
 * Uses regular expressions to extract pinned Go versions and toolchain specifications.
 */
class GoModDirectiveReader(
    private val fs: FileSystemAccess = RealFileSystemAccess()
) {
    companion object {
        private val LOG = Logger.getInstance(GoModDirectiveReader::class.java)

        private val TOOLCHAIN_REGEX = Regex("""^toolchain\s+go([\d.]+)""", RegexOption.MULTILINE)
        private val GO_DIRECTIVE_REGEX = Regex("""^go\s+([\d.]+)""", RegexOption.MULTILINE)
        private val GO_VERSION_FILE_REGEX = """^(?:go)?(\d+\.\d+(?:\.\d+)?)""".toRegex()

        private val DEFAULT = GoModDirectiveReader()

        fun readGoDirective(file: File): String? = DEFAULT.readGoDirective(file.absolutePath)
        fun readGoVersionFile(file: File): String? = DEFAULT.readGoVersionFile(file.absolutePath)
    }

    fun readGoDirective(path: String): String? {
        val content = fs.readText(path) ?: return null
        val toolchainMatch = TOOLCHAIN_REGEX.find(content)
        val goMatch = GO_DIRECTIVE_REGEX.find(content)
        return toolchainMatch?.groupValues?.get(1) ?: goMatch?.groupValues?.get(1)
    }

    fun readGoVersionFile(path: String): String? {
        if (!fs.exists(path)) return null
        return try {
            val content = fs.readText(path)?.trim() ?: return null
            GO_VERSION_FILE_REGEX.find(content)?.groupValues?.get(1)
        } catch (e: Exception) {
            LOG.debug("Failed to read .go-version file at $path: ${e.message}")
            null
        }
    }
}
