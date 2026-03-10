package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.SystemInfo
import java.io.File

object GoSdkUtils {
    val GO_EXECUTABLE_NAME = if (SystemInfo.isWindows) "go.exe" else "go"
    val GOPLS_EXECUTABLE_NAME = if (SystemInfo.isWindows) "gopls.exe" else "gopls"
    private val versionCache = mutableMapOf<String, String>()

    fun getGoExecutablePath(sdkHomePath: String): String? {
        val binDir = File(sdkHomePath, "bin")
        val goExe = File(binDir, GO_EXECUTABLE_NAME)
        return if (goExe.exists() && goExe.canExecute()) goExe.absolutePath else null
    }

    fun getGoplsExecutablePath(sdkHomePath: String?): String? {
        // 1. Try relative to SDK home (some distributions bundle it)
        if (sdkHomePath != null) {
            val binDir = File(sdkHomePath, "bin")
            val goplsExe = File(binDir, GOPLS_EXECUTABLE_NAME)
            if (goplsExe.exists() && goplsExe.canExecute()) return goplsExe.absolutePath
        }

        // 2. Try GOPATH/bin
        val gopath = System.getenv("GOPATH")
        if (gopath != null) {
            val gopathBin = File(File(gopath, "bin"), GOPLS_EXECUTABLE_NAME)
            if (gopathBin.exists() && gopathBin.canExecute()) return gopathBin.absolutePath
        }

        // 3. Try user home go/bin (default GOPATH)
        val userHomeGoBin = File(File(System.getProperty("user.home"), "go/bin"), GOPLS_EXECUTABLE_NAME)
        if (userHomeGoBin.exists() && userHomeGoBin.canExecute()) return userHomeGoBin.absolutePath

        // 4. Fallback to system PATH
        return GOPLS_EXECUTABLE_NAME
    }

    fun isValidSdkHome(path: String): Boolean {
        return getGoExecutablePath(path) != null
    }

    fun retrieveGoVersion(sdkHomePath: String): String? {
        versionCache[sdkHomePath]?.let { return it }

        val exePath = getGoExecutablePath(sdkHomePath) ?: return null
        
        if (ApplicationManager.getApplication().isDispatchThread && !versionCache.containsKey(sdkHomePath)) {
            return null 
        }

        try {
            val commandLine = GeneralCommandLine(exePath, "version")
            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess(5000)

            if (output.exitCode == 0) {
                val stdout = output.stdout
                val regex = """go version go(\d+\.\d+(\.\d+)?)""".toRegex()
                val match = regex.find(stdout)
                val version = match?.groupValues?.get(1)
                if (version != null) {
                    versionCache[sdkHomePath] = version
                    return version
                }
            }
        } catch (e: Exception) {
            // Log error
        }
        return null
    }

    fun suggestSdkHomePaths(): List<String> {
        val suggestions = mutableListOf<String>()

        // 1. Check GOROOT
        System.getenv("GOROOT")?.let { if (isValidSdkHome(it)) suggestions.add(it) }

        // 2. Common OS paths
        if (SystemInfo.isWindows) {
            listOf("C:\\Go", "C:\\Program Files\\Go").forEach {
                if (isValidSdkHome(it)) suggestions.add(it)
            }
        } else {
            listOf("/usr/local/go", "/usr/bin/go", "/opt/homebrew/bin/go").forEach {
                val file = File(it)
                if (file.exists()) {
                    val home = if (file.isDirectory) file.absolutePath else file.parentFile.parentFile?.absolutePath
                    if (home != null && isValidSdkHome(home)) suggestions.add(home)
                }
            }
        }

        // Sort by version descending to get the latest first
        return suggestions.distinct().sortedByDescending { retrieveGoVersion(it) ?: "" }
    }
}
