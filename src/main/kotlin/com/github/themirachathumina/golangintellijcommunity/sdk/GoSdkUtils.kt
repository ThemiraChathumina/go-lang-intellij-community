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
    private val versionCache = mutableMapOf<String, String>()

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
        println("Retrieving GO version $sdkHomePath")
        val normalizedPath = FileUtil.toSystemIndependentName(sdkHomePath)

        versionCache[normalizedPath]?.let { return it }

        val exePath = getGoExecutablePath(sdkHomePath)
        if (exePath == null) {
            println("Go executable not found")
            LOG.warn("Cannot retrieve Go version: Go executable not found at $sdkHomePath")
            return null
        }

        if (ApplicationManager.getApplication().isDispatchThread && !versionCache.containsKey(normalizedPath)) {
            println("retrieveGoVersion called from EDT for non-cached path: $normalizedPath. Returning null to avoid UI freeze.")
            LOG.debug("retrieveGoVersion called from EDT for non-cached path: $normalizedPath. Returning null to avoid UI freeze.")
            return null
        }

        try {
            println("Running '$exePath version' to retrieve Go version...")
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
                    println("Detected Go version $version for $sdkHomePath")
                    LOG.info("Detected Go version $version for $sdkHomePath")
                    versionCache[normalizedPath] = version
                    return version
                } else {
                    println("Failed to parse Go version from output: $stdout")
                    LOG.warn("Failed to parse Go version from output: $stdout")
                }
            } else {
                println("'go version' failed with exit code ${output.exitCode}. stderr: ${output.stderr}")
                LOG.warn("'go version' failed with exit code ${output.exitCode}. stderr: ${output.stderr}")
            }
        } catch (e: Exception) {
            println("Error retrieving Go version for $sdkHomePath")
            LOG.error("Error retrieving Go version for $sdkHomePath", e)
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

        val result = suggestions.distinct().sortedByDescending { retrieveGoVersion(it) ?: "" }
        LOG.info("Suggested SDK paths: $result")
        return result
    }
}