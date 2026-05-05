package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

/**
 * Scans console output for Go file locations (e.g. "main_test.go:88") and
 * turns them into clickable hyperlinks.
 *
 * All matches on a single line are hyperlinked.  File resolution order:
 *   1. Absolute path (used as-is)
 *   2. Relative to [workingDirectory]
 *   3. Project-wide search by filename (covers per-test output panes where the
 *      working directory is unknown)
 */
class GoConsoleFilter(
    private val project: Project,
    private val workingDirectory: String = project.basePath ?: ""
) : Filter {

    companion object {
        // Matches "some_file.go:42" – with or without a leading absolute path segment
        private val FILE_LINE_PATTERN = Regex("""((?:[a-zA-Z]:[\\/])?[\w\-.\\/]+?\.go):(\d+)""")
    }

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val matches = FILE_LINE_PATTERN.findAll(line).toList()
        if (matches.isEmpty()) return null

        val textStartOffset = entireLength - line.length
        val items = matches.mapNotNull { match ->
            val fileStr = match.groupValues[1]
            val lineNum = match.groupValues[2].toIntOrNull() ?: return@mapNotNull null
            val vFile   = resolveFile(fileStr) ?: return@mapNotNull null

            Filter.ResultItem(
                textStartOffset + match.range.first,
                textStartOffset + match.range.last + 1,
                OpenFileHyperlinkInfo(project, vFile, lineNum - 1, 0)
            )
        }
        return if (items.isEmpty()) null else Filter.Result(items)
    }

    private fun resolveFile(fileStr: String): VirtualFile? {
        val lfs = LocalFileSystem.getInstance()

        // 1. Absolute path
        if (File(fileStr).isAbsolute) {
            return lfs.findFileByPath(fileStr.replace('\\', '/'))
        }

        // 2. Relative to the working directory (most precise when available)
        val direct = lfs.findFileByPath(
            File(workingDirectory, fileStr).absolutePath.replace('\\', '/')
        )
        if (direct != null) return direct

        // 3. Project-wide search by base filename
        val fileName = File(fileStr).name
        return ApplicationManager.getApplication().runReadAction<VirtualFile?> {
            FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.projectScope(project))
                .firstOrNull()
        }
    }
}
