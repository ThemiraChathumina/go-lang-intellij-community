package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import java.io.File

object GoSdkUtils {
    private val LOG = Logger.getInstance(GoSdkUtils::class.java)
    val GO_EXECUTABLE_NAME = if (SystemInfo.isWindows) "go.exe" else "go"
    val GOPLS_EXECUTABLE_NAME = if (SystemInfo.isWindows) "gopls.exe" else "gopls"
    const val MIN_SUPPORTED_VERSION = "1.21"

    private val versionCache = mutableMapOf<String, String>()
    private val pendingVersions = mutableSetOf<String>()

    fun getGoExecutablePath(sdkHomePath: String): String? {
        val binDir = File(sdkHomePath, "bin")
        val goExe = File(binDir, GO_EXECUTABLE_NAME)
        val exists = goExe.exists() && goExe.canExecute()
        if (!exists) {
            LOG.debug("[Go-Lang-Community] Go executable not found or not executable at: ${goExe.absolutePath}")
        }
        return if (exists) goExe.absolutePath else null
    }

    fun getGoplsExecutablePath(sdkHomePath: String?): String? {
        if (sdkHomePath != null) {
            val binDir = File(sdkHomePath, "bin")
            val goplsExe = File(binDir, GOPLS_EXECUTABLE_NAME)
            if (goplsExe.exists() && goplsExe.canExecute()) {
                LOG.debug("Found gopls in SDK bin: ${goplsExe.absolutePath}")
                return goplsExe.absolutePath
            }
        }

        val gopath = System.getenv("GOPATH")
        if (gopath != null) {
            val gopathBin = File(File(gopath, "bin"), GOPLS_EXECUTABLE_NAME)
            if (gopathBin.exists() && gopathBin.canExecute()) {
                LOG.debug("Found gopls in GOPATH bin: ${gopathBin.absolutePath}")
                return gopathBin.absolutePath
            }
        }

        val userHomeGoBin = File(File(System.getProperty("user.home"), "go/bin"), GOPLS_EXECUTABLE_NAME)
        if (userHomeGoBin.exists() && userHomeGoBin.canExecute()) {
            LOG.debug("Found gopls in user home go/bin: ${userHomeGoBin.absolutePath}")
            return userHomeGoBin.absolutePath
        }

        LOG.debug("gopls not found in standard locations, falling back to: $GOPLS_EXECUTABLE_NAME")
        return GOPLS_EXECUTABLE_NAME
    }

    fun isGoplsInstalled(sdkHomePath: String?): Boolean {
        val path = getGoplsExecutablePath(sdkHomePath)

        // If it found a concrete file path that exists, we are good!
        if (path != GOPLS_EXECUTABLE_NAME && File(path ?: "").exists()) return true

        // Fallback: If it just returned "gopls", try executing it to see if it's in the system PATH
        return try {
            val commandLine = GeneralCommandLine(GOPLS_EXECUTABLE_NAME, "version")
            val handler = CapturingProcessHandler(commandLine)
            handler.runProcess(2000).exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    fun isValidSdkHome(path: String): Boolean {
        val isValid = getGoExecutablePath(path) != null
        LOG.debug("Checking if $path is a valid SDK home: $isValid")
        return isValid
    }

    fun retrieveGoVersion(sdkHomePath: String): String? {
        val normalizedPath = FileUtil.toSystemIndependentName(sdkHomePath)
        versionCache[normalizedPath]?.let { return it }

        val exePath = getGoExecutablePath(sdkHomePath)
        if (exePath == null) return null

        if (ApplicationManager.getApplication().isDispatchThread) {
            if (!pendingVersions.contains(normalizedPath)) {
                pendingVersions.add(normalizedPath)
                LOG.info("[Go-Lang-Community] Scheduling background version retrieval for: $normalizedPath")
                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        doRetrieveGoVersion(normalizedPath, exePath)
                    } finally {
                        pendingVersions.remove(normalizedPath)
                        // Notify registry to refresh UI
                        ApplicationManager.getApplication().invokeLater({
                            ApplicationManager.getApplication().messageBus.syncPublisher(GoGlobalSdkRegistry.TOPIC).sdkRegistryChanged()
                        }, com.intellij.openapi.application.ModalityState.any())
                    }
                }
            }
            return null
        }

        return doRetrieveGoVersion(normalizedPath, exePath)
    }

    private fun doRetrieveGoVersion(normalizedPath: String, exePath: String): String? {
        try {
            LOG.debug("Running '$exePath version' to retrieve Go version...")
            val commandLine = GeneralCommandLine(exePath, "version")
            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess(5000)

            if (output.exitCode == 0) {
                val stdout = output.stdout
                val regex = """go version go(\d+\.\d+(\.\d+)?)""".toRegex()
                val match = regex.find(stdout)
                val version = match?.groupValues?.get(1)
                if (version != null) {
                    LOG.info("Detected Go version $version for $normalizedPath")
                    versionCache[normalizedPath] = version
                    return version
                }
            }
        } catch (e: Exception) {
            LOG.error("Error retrieving Go version for $normalizedPath", e)
        }
        return null
    }

    fun suggestSdkHomePaths(): List<String> {
        LOG.debug("Suggesting SDK home paths...")
        val suggestions = mutableListOf<String>()

        System.getenv("GOROOT")?.let {
            LOG.debug("Checking GOROOT: $it")
            if (isValidSdkHome(it)) suggestions.add(it)
        }

        System.getenv("PATH")?.split(File.pathSeparator)?.forEach { pathEntry ->
            val dir = File(pathEntry)
            // Look for a 'bin' directory where the path contains 'go' (e.g., C:\Program Files\Go\bin)
            if (dir.exists() && dir.isDirectory &&
                dir.name.equals("bin", ignoreCase = true) &&
                dir.absolutePath.contains("go", ignoreCase = true)) {

                // The actual SDK Home is the parent of the 'bin' directory
                val parentPath = dir.parentFile?.absolutePath
                if (parentPath != null && isValidSdkHome(parentPath)) {
                    println("Found valid home from PATH entry $pathEntry: $parentPath")
                    LOG.debug("Found valid home from PATH entry $pathEntry: $parentPath")
                    suggestions.add(parentPath)
                }
            }
        }

        // Check ~/sdk for SDKs downloaded by Go toolchain management (e.g. ~/sdk/go1.22.0)
        val userSdkDir = File(System.getProperty("user.home"), "sdk")
        if (userSdkDir.isDirectory) {
            userSdkDir.listFiles()?.filter { it.isDirectory && it.name.startsWith("go") }?.forEach {
                LOG.debug("Checking user SDK dir: ${it.absolutePath}")
                if (isValidSdkHome(it.absolutePath)) suggestions.add(it.absolutePath)
            }
        }

        if (SystemInfo.isWindows) {
            listOf("C:\\Go", "C:\\Program Files\\Go").forEach {
                LOG.debug("Checking Windows path: $it")
                if (isValidSdkHome(it)) suggestions.add(it)
            }
        } else {
            listOf("/usr/local/go", "/usr/bin/go", "/opt/homebrew/bin/go").forEach {
                LOG.debug("Checking Unix path: $it")
                val file = File(it)
                if (file.exists()) {
                    val home = if (file.isDirectory) file.absolutePath else file.parentFile.parentFile?.absolutePath
                    if (home != null && isValidSdkHome(home)) {
                        LOG.debug("Found valid home from path $it: $home")
                        suggestions.add(home)
                    }
                }
            }
        }

        val result = suggestions.distinct()
        LOG.info("Suggested SDK paths: $result")
        return result
    }

    fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }

    fun isSupportedVersion(version: String): Boolean {
        return compareVersions(version, MIN_SUPPORTED_VERSION) >= 0
    }
}
