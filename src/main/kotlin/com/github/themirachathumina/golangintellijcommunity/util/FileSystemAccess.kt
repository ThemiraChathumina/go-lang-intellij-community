package com.github.themirachathumina.golangintellijcommunity.util

import com.intellij.openapi.util.io.FileUtil
import java.io.File

interface FileSystemAccess {
    fun exists(path: String): Boolean
    fun isDirectory(path: String): Boolean
    fun isFile(path: String): Boolean
    fun canExecute(path: String): Boolean
    fun readText(path: String): String?
    fun listChildren(path: String): List<String>
    fun getParent(path: String): String?
    fun getName(path: String): String
    fun combine(vararg parts: String): String
    fun getUserHome(): String
    fun getSystemProperty(key: String): String?
    fun getEnv(key: String): String?
    fun mkdirs(path: String): Boolean
    fun delete(path: String): Boolean
    fun deleteRecursively(path: String): Boolean
    fun renameTo(source: String, destination: String): Boolean
    fun walk(path: String, maxDepth: Int = Int.MAX_VALUE): Sequence<String>
}

class RealFileSystemAccess : FileSystemAccess {
    override fun exists(path: String): Boolean = File(FileUtil.toSystemDependentName(path)).exists()
    override fun isDirectory(path: String): Boolean = File(FileUtil.toSystemDependentName(path)).isDirectory
    override fun isFile(path: String): Boolean = File(FileUtil.toSystemDependentName(path)).isFile
    override fun canExecute(path: String): Boolean = File(FileUtil.toSystemDependentName(path)).canExecute()
    override fun readText(path: String): String? = try {
        File(FileUtil.toSystemDependentName(path)).readText()
    } catch (e: Exception) {
        null
    }

    override fun listChildren(path: String): List<String> =
        File(FileUtil.toSystemDependentName(path)).listFiles()?.map { it.absolutePath } ?: emptyList()

    override fun getParent(path: String): String? = File(FileUtil.toSystemDependentName(path)).parent
    override fun getName(path: String): String = File(FileUtil.toSystemDependentName(path)).name

    override fun combine(vararg parts: String): String {
        var result = File(FileUtil.toSystemDependentName(parts[0]))
        for (i in 1 until parts.size) {
            result = File(result, parts[i])
        }
        return FileUtil.toSystemIndependentName(result.absolutePath)
    }

    override fun getUserHome(): String = System.getProperty("user.home")
    override fun getSystemProperty(key: String): String? = System.getProperty(key)
    override fun getEnv(key: String): String? = System.getenv(key)

    override fun mkdirs(path: String): Boolean = File(FileUtil.toSystemDependentName(path)).mkdirs()
    override fun delete(path: String): Boolean = File(FileUtil.toSystemDependentName(path)).delete()
    override fun deleteRecursively(path: String): Boolean = File(FileUtil.toSystemDependentName(path)).deleteRecursively()
    override fun renameTo(source: String, destination: String): Boolean =
        File(FileUtil.toSystemDependentName(source)).renameTo(File(FileUtil.toSystemDependentName(destination)))

    override fun walk(path: String, maxDepth: Int): Sequence<String> =
        File(FileUtil.toSystemDependentName(path)).walkTopDown().maxDepth(maxDepth).map { it.absolutePath }
}
