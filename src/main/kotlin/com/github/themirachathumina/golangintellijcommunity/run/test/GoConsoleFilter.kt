package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

/**
 * Scans console output for Go error locations (e.g. "main_test.go:88") and
 * turns them into clickable hyperlinks.
 */
class GoConsoleFilter(
    private val project: Project,
    private val workingDirectory: String
) : Filter {

    // Matches paths like "main_test.go:88" or "C:\foo\bar_test.go:12"
    private val pattern = Regex("""((?:[a-zA-Z]:[\\/])?[\w\-.\\/]+?\.go):(\d+)""")

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val match = pattern.find(line) ?: return null
        val fileStr = match.groupValues[1]
        val lineNum = match.groupValues[2].toIntOrNull() ?: return null

        val file = if (File(fileStr).isAbsolute) {
            LocalFileSystem.getInstance().findFileByPath(fileStr.replace('\\', '/'))
        } else {
            LocalFileSystem.getInstance().findFileByPath(
                File(workingDirectory, fileStr).absolutePath.replace('\\', '/')
            )
        } ?: return null

        val textStartOffset = entireLength - line.length

        // match.range contains the precise bounds for the entire "main_test.go:88" match
        val matchStart = textStartOffset + match.range.first
        val matchEnd = textStartOffset + match.range.last + 1

        // Line numbers are 0-indexed in the IntelliJ VFS
        val hyperlinkInfo = OpenFileHyperlinkInfo(project, file, lineNum - 1, 0)

        return Filter.Result(matchStart, matchEnd, hyperlinkInfo)
    }
}