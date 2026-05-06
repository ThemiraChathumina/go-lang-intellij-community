package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.util.FileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.RealFileSystemAccess
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * Lightweight helpers for reading basic metadata from a project's go.mod file.
 * Results are NOT cached – go.mod edits are rare enough that re-reading on
 * each coverage annotation pass is acceptable.
 */
class GoModUtils(private val fs: FileSystemAccess = RealFileSystemAccess()) {

    companion object {
        private val DEFAULT = GoModUtils()
        
        fun getModuleName(project: Project): String? = DEFAULT.getModuleName(project)
        fun getModuleNameForFile(file: VirtualFile): String? = DEFAULT.getModuleNameForFile(file)
        fun getModuleRootDir(project: Project): File? = DEFAULT.getModuleRootDir(project)
        fun getModuleRootDirForFile(file: VirtualFile): File? = DEFAULT.getModuleRootDirForFile(file)
    }

    /** Returns the module name declared in go.mod (e.g. "github.com/user/myproject"). */
    fun getModuleName(project: Project): String? {
        val goMod = findGoMod(project) ?: return null
        return readModuleName(goMod)
    }

    /** Returns the module name for a specific file by looking up for go.mod. */
    fun getModuleNameForFile(file: VirtualFile): String? {
        val goMod = findGoModForFile(file.path) ?: return null
        return readModuleName(goMod)
    }

    /** Returns the directory that contains go.mod (the module root). */
    fun getModuleRootDir(project: Project): File? = findGoMod(project)?.let { File(fs.getParent(it)) }

    /** Returns the module root directory for a specific file. */
    fun getModuleRootDirForFile(file: VirtualFile): File? = findGoModForFile(file.path)?.let { File(fs.getParent(it)) }

    private fun readModuleName(goModPath: String): String? {
        val text = fs.readText(goModPath) ?: return null
        return text.lines()
            .map { it.trim() }
            .firstOrNull { it.startsWith("module ") }
            ?.removePrefix("module ")
            ?.trim()
    }

    private fun findGoMod(project: Project): String? {
        val base = project.basePath ?: return null
        val candidate = fs.combine(base, GoConstants.FileExtensions.MOD)
        if (fs.exists(candidate)) return candidate

        // Fallback: search one level deep if not in root (common in monorepos)
        return fs.listChildren(base).filter { fs.isDirectory(it) }
            .map { fs.combine(it, GoConstants.FileExtensions.MOD) }
            .firstOrNull { fs.exists(it) }
    }

    private fun findGoModForFile(filePath: String): String? {
        var current: String? = fs.getParent(filePath)
        while (current != null) {
            val candidate = fs.combine(current, GoConstants.FileExtensions.MOD)
            if (fs.exists(candidate)) return candidate
            current = fs.getParent(current)
        }
        return null
    }
}
