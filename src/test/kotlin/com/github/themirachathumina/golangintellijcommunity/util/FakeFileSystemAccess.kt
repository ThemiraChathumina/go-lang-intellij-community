package com.github.themirachathumina.golangintellijcommunity.util

class FakeFileSystemAccess(
    val files: MutableMap<String, String> = mutableMapOf(),
    val directories: MutableSet<String> = mutableSetOf(),
    val executables: MutableSet<String> = mutableSetOf(),
    var userHomePath: String = "/home/user",
    val env: MutableMap<String, String> = mutableMapOf(),
    val systemProperties: MutableMap<String, String> = mutableMapOf()
) : FileSystemAccess {
    override fun exists(path: String): Boolean = files.containsKey(path) || directories.contains(path)
    override fun isDirectory(path: String): Boolean = directories.contains(path)
    override fun isFile(path: String): Boolean = files.containsKey(path)
    override fun canExecute(path: String): Boolean = executables.contains(path)
    override fun readText(path: String): String? = files[path]
    override fun listChildren(path: String): List<String> =
        (files.keys + directories).filter { it.startsWith("$path/") && it.substring(path.length + 1).indexOf('/') == -1 }

    override fun getParent(path: String): String? = path.substringBeforeLast('/', "").takeIf { it.isNotEmpty() }
    override fun getName(path: String): String = path.substringAfterLast('/')

    override fun combine(vararg parts: String): String = parts.filter { it.isNotEmpty() }.joinToString("/")
    override fun getUserHome(): String = userHomePath
    override fun getSystemProperty(key: String): String? = systemProperties[key]
    override fun getEnv(key: String): String? = env[key]

    override fun mkdirs(path: String): Boolean {
        directories.add(path)
        return true
    }

    override fun delete(path: String): Boolean {
        files.remove(path)
        directories.remove(path)
        return true
    }

    override fun deleteRecursively(path: String): Boolean {
        files.keys.removeIf { it.startsWith(path) }
        directories.removeIf { it.startsWith(path) }
        return true
    }

    override fun renameTo(source: String, destination: String): Boolean {
        if (files.containsKey(source)) {
            files[destination] = files.remove(source)!!
        } else if (directories.contains(source)) {
            directories.remove(source)
            directories.add(destination)
            // Should also move children, but keeping it simple for now
        }
        return true
    }

    override fun walk(path: String, maxDepth: Int): Sequence<String> =
        (files.keys + directories).filter { it.startsWith(path) }.asSequence()
}
