package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * Lightweight helpers for reading basic metadata from a project's go.mod file.
 * Results are NOT cached – go.mod edits are rare enough that re-reading on
 * each coverage annotation pass is acceptable.
 */
object GoModUtils {

    /** Returns the module name declared in go.mod (e.g. "github.com/user/myproject"). */
    fun getModuleName(project: Project): String? {
        val goMod = findGoMod(project) ?: return null
        return readModuleName(goMod)
    }

    /** Returns the module name for a specific file by looking up for go.mod. */
    fun getModuleNameForFile(file: VirtualFile): String? {
        val goMod = findGoModForFile(file) ?: return null
        return readModuleName(goMod)
    }

    /** Returns the directory that contains go.mod (the module root). */
    fun getModuleRootDir(project: Project): File? = findGoMod(project)?.parentFile

    /** Returns the module root directory for a specific file. */
    fun getModuleRootDirForFile(file: VirtualFile): File? = findGoModForFile(file)?.parentFile

    private fun readModuleName(goMod: File): String? {
        return goMod.bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .firstOrNull { it.startsWith("module ") }
                ?.removePrefix("module ")
                ?.trim()
        }
    }

    private fun findGoMod(project: Project): File? {
        val base = project.basePath ?: return null
        val candidate = File(base, GoConstants.FileExtensions.MOD)
        if (candidate.exists()) return candidate

        // Fallback: search one level deep if not in root (common in monorepos)
        return File(base).listFiles()?.filter { it.isDirectory }
            ?.map { File(it, GoConstants.FileExtensions.MOD) }
            ?.firstOrNull { it.exists() }
    }

    private fun findGoModForFile(file: VirtualFile): File? {
        var current = File(file.path).parentFile
        while (current != null) {
            val candidate = File(current, GoConstants.FileExtensions.MOD)
            if (candidate.exists()) return candidate
            current = current.parentFile
        }
        return null
    }
}
