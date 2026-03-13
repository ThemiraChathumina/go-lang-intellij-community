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

        // Collect all GOPATH entries: system env, Go env file, and default ~/go — split on path separator
        val gopathEntries = buildList {
            val goEnv = readGoEnvFile()
            goEnv["GOPATH"]?.split(File.pathSeparator)?.forEach { add(it) }
            System.getenv("GOPATH")?.split(File.pathSeparator)?.forEach { add(it) }
            add(File(System.getProperty("user.home"), "go").absolutePath)
        }.distinct()

        for (gopathEntry in gopathEntries) {
            val gopathBin = File(File(gopathEntry, "bin"), GOPLS_EXECUTABLE_NAME)
            if (gopathBin.exists() && gopathBin.canExecute()) {
                LOG.debug("Found gopls in GOPATH bin: ${gopathBin.absolutePath}")
                return gopathBin.absolutePath
            }
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

    private fun readVersionFile(sdkHomePath: String): String? {
        val versionFile = File(sdkHomePath, "VERSION")
        if (!versionFile.exists()) return null
        return try {
            // Format: "go1.22.3" or "go1.22.3 cl/123456" or "devel ..." for tip builds
            val content = versionFile.readText().trim()
            val regex = """^go(\d+\.\d+(\.\d+)?)""".toRegex()
            regex.find(content)?.groupValues?.get(1)
        } catch (e: Exception) {
            LOG.debug("Failed to read VERSION file at $sdkHomePath: ${e.message}")
            null
        }
    }

    fun retrieveGoVersion(sdkHomePath: String): String? {
        val normalizedPath = FileUtil.toSystemIndependentName(sdkHomePath)
        versionCache[normalizedPath]?.let { return it }

        // Fast path: read <GOROOT>/VERSION — no process spawn, works on any thread
        val versionFromFile = readVersionFile(sdkHomePath)
        if (versionFromFile != null) {
            LOG.debug("Read Go version $versionFromFile from VERSION file for $normalizedPath")
            versionCache[normalizedPath] = versionFromFile
            return versionFromFile
        }

        // Slow path: fall back to spawning `go version` for non-standard installations
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

    /**
     * Reads the per-user Go environment file (written by `go env -w`).
     * Location: $GOENV, or ~/.config/go/env (Linux/Mac), or %AppData%\go\env (Windows).
     * Returns an empty map if the file is absent, unreadable, or GOENV=off.
     */
    fun readGoEnvFile(): Map<String, String> {
        val goenvVar = System.getenv("GOENV")
        if (goenvVar == "off") return emptyMap()

        val envFile = when {
            goenvVar != null -> File(goenvVar)
            SystemInfo.isWindows -> File(System.getenv("AppData") ?: return emptyMap(), "go/env")
            else -> {
                val xdgConfig = System.getenv("XDG_CONFIG_HOME")
                if (xdgConfig != null) File(xdgConfig, "go/env")
                else File(System.getProperty("user.home"), ".config/go/env")
            }
        }

        if (!envFile.exists()) return emptyMap()
        return try {
            envFile.readLines()
                .filter { it.isNotBlank() && !it.startsWith('#') && '=' in it }
                .associate { line ->
                    val idx = line.indexOf('=')
                    line.substring(0, idx).trim() to line.substring(idx + 1).trim()
                }
        } catch (e: Exception) {
            LOG.debug("Failed to read Go env file at ${envFile.absolutePath}: ${e.message}")
            emptyMap()
        }
    }

    fun suggestSdkHomePaths(): List<String> {
        LOG.debug("Suggesting SDK home paths...")
        val suggestions = mutableListOf<String>()
        val home = System.getProperty("user.home")
        val goEnv = readGoEnvFile()

        // --- Go env file overrides (highest priority — mirrors how Go tools resolve env) ---

        goEnv["GOROOT"]?.let {
            LOG.debug("Checking GOROOT from Go env file: $it")
            if (isValidSdkHome(it)) suggestions.add(it)
        }

        // All GOPATH entries from the env file may contain sdk/ subdirs with downloaded toolchains
        goEnv["GOPATH"]?.split(File.pathSeparator)?.forEach { gopathEntry ->
            scanVersionedDir(File(gopathEntry, "sdk"), prefix = "go", suggestions)
        }

        // --- System environment variables ---

        System.getenv("GOROOT")?.let {
            LOG.debug("Checking GOROOT: $it")
            if (isValidSdkHome(it)) suggestions.add(it)
        }

        // PATH entries whose parent looks like a Go SDK home (e.g. C:\Program Files\Go\bin)
        System.getenv("PATH")?.split(File.pathSeparator)?.forEach { pathEntry ->
            val dir = File(pathEntry)
            if (dir.exists() && dir.isDirectory &&
                dir.name.equals("bin", ignoreCase = true) &&
                dir.absolutePath.contains("go", ignoreCase = true)) {
                val parentPath = dir.parentFile?.absolutePath
                if (parentPath != null && isValidSdkHome(parentPath)) {
                    LOG.debug("Found valid home from PATH entry $pathEntry: $parentPath")
                    suggestions.add(parentPath)
                }
            }
        }

        // --- Go toolchain management: ~/sdk/go* ---
        scanVersionedDir(File(home, "sdk"), prefix = "go", suggestions)

        // --- Version managers ---

        // gvm: ~/.gvm/gos/go*
        scanVersionedDir(File(home, ".gvm/gos"), prefix = "go", suggestions)

        // goenv: ~/.goenv/versions/*
        scanVersionedDir(File(home, ".goenv/versions"), prefix = "", suggestions)

        // asdf: ~/.asdf/installs/golang/*/go
        val asdfGolangDir = File(home, ".asdf/installs/golang")
        if (asdfGolangDir.isDirectory) {
            asdfGolangDir.listFiles()?.filter { it.isDirectory }?.forEach { versionDir ->
                // asdf wraps the SDK in a 'go' subdirectory
                val sdkHome = File(versionDir, "go")
                val path = if (sdkHome.isDirectory) sdkHome.absolutePath else versionDir.absolutePath
                LOG.debug("Checking asdf golang install: $path")
                if (isValidSdkHome(path)) suggestions.add(path)
            }
        }

        // mise (formerly rtx): ~/.local/share/mise/installs/go/*
        scanVersionedDir(File(home, ".local/share/mise/installs/go"), prefix = "", suggestions)

        if (SystemInfo.isWindows) {
            // Standard installer locations
            listOf("C:\\Go", "C:\\Program Files\\Go").forEach {
                LOG.debug("Checking Windows path: $it")
                if (isValidSdkHome(it)) suggestions.add(it)
            }

            // Scoop: ~/scoop/apps/go/current
            val scoopCurrent = File(home, "scoop/apps/go/current")
            LOG.debug("Checking Scoop: ${scoopCurrent.absolutePath}")
            if (isValidSdkHome(scoopCurrent.absolutePath)) suggestions.add(scoopCurrent.absolutePath)

            // Chocolatey: C:\ProgramData\chocolatey\lib\golang\tools
            val chocoPath = "C:\\ProgramData\\chocolatey\\lib\\golang\\tools"
            LOG.debug("Checking Chocolatey: $chocoPath")
            if (isValidSdkHome(chocoPath)) suggestions.add(chocoPath)

        } else {
            // Standard Unix locations
            listOf("/usr/local/go", "/usr/lib/go").forEach {
                LOG.debug("Checking Unix path: $it")
                if (isValidSdkHome(it)) suggestions.add(it)
            }

            if (SystemInfo.isMac) {
                // Homebrew on Apple Silicon: /opt/homebrew/Cellar/go/<version>/libexec
                // Homebrew on Intel Mac:     /usr/local/Cellar/go/<version>/libexec
                listOf("/opt/homebrew/Cellar/go", "/usr/local/Cellar/go").forEach { cellar ->
                    scanVersionedDir(File(cellar), prefix = "", suggestions, subDir = "libexec")
                }
            }

            // snap: /snap/go/current
            val snapPath = "/snap/go/current"
            LOG.debug("Checking snap: $snapPath")
            if (isValidSdkHome(snapPath)) suggestions.add(snapPath)
        }

        val result = suggestions.distinct()
        LOG.info("Suggested SDK paths: $result")
        return result
    }

    /**
     * Lists immediate subdirectories of [dir] whose names start with [prefix],
     * optionally descending into [subDir] within each, and adds valid SDK homes
     * to [suggestions].
     */
    private fun scanVersionedDir(dir: File, prefix: String, suggestions: MutableList<String>, subDir: String? = null) {
        if (!dir.isDirectory) return
        dir.listFiles()?.filter { it.isDirectory && it.name.startsWith(prefix) }?.forEach { entry ->
            val candidate = if (subDir != null) File(entry, subDir) else entry
            LOG.debug("Checking versioned SDK dir: ${candidate.absolutePath}")
            if (isValidSdkHome(candidate.absolutePath)) suggestions.add(candidate.absolutePath)
        }
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

    fun readGoDirective(file: File): String? {
        val content = file.readText()
        val toolchainMatch = Regex("""^toolchain\s+go([\d.]+)""", RegexOption.MULTILINE).find(content)
        val goMatch = Regex("""^go\s+([\d.]+)""", RegexOption.MULTILINE).find(content)
        return toolchainMatch?.groupValues?.get(1) ?: goMatch?.groupValues?.get(1)
    }

    fun isSupportedVersion(version: String): Boolean {
        return compareVersions(version, MIN_SUPPORTED_VERSION) >= 0
    }
}
