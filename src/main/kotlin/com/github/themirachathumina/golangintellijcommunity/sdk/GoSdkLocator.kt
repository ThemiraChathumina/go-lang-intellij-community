package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import java.io.File

object GoSdkLocator {
    private val LOG = Logger.getInstance(GoSdkLocator::class.java)

    val GO_EXECUTABLE_NAME = if (SystemInfo.isWindows) "go.exe" else GoConstants.Executables.GO
    val GOPLS_EXECUTABLE_NAME = if (SystemInfo.isWindows) "gopls.exe" else GoConstants.Executables.GOPLS

    fun getGoExecutablePath(sdkHomePath: String): String? {
        val normalized = FileUtil.toSystemDependentName(sdkHomePath)
        val binDir = File(normalized, "bin")
        val goExe = File(binDir, GO_EXECUTABLE_NAME)
        val exists = goExe.exists() && goExe.canExecute()
        if (!exists) {
            LOG.debug("${GoConstants.Log.PREFIX} Go executable not found or not executable at: ${goExe.absolutePath}")
        }
        return if (exists) FileUtil.toSystemIndependentName(goExe.absolutePath) else null
    }

    fun getGoplsExecutablePath(sdkHomePath: String?): String? {
        if (sdkHomePath != null) {
            val normalized = FileUtil.toSystemDependentName(sdkHomePath)
            val binDir = File(normalized, "bin")
            val goplsExe = File(binDir, GOPLS_EXECUTABLE_NAME)
            if (goplsExe.exists() && goplsExe.canExecute()) {
                LOG.debug("Found gopls in SDK bin: ${goplsExe.absolutePath}")
                return FileUtil.toSystemIndependentName(goplsExe.absolutePath)
            }
        }

        val gopathEntries = buildList {
            val goEnv = GoEnvironmentReader.readGoEnvFile()
            goEnv[GoConstants.Env.GOPATH]?.split(File.pathSeparator)?.forEach { add(it) }
            System.getenv(GoConstants.Env.GOPATH)?.split(File.pathSeparator)?.forEach { add(it) }
            add(File(System.getProperty("user.home"), "go").absolutePath)
        }.distinct()

        for (gopathEntry in gopathEntries) {
            val gopathBin = File(File(gopathEntry, "bin"), GOPLS_EXECUTABLE_NAME)
            if (gopathBin.exists() && gopathBin.canExecute()) {
                LOG.debug("Found gopls in GOPATH bin: ${gopathBin.absolutePath}")
                return FileUtil.toSystemIndependentName(gopathBin.absolutePath)
            }
        }

        LOG.debug("gopls not found in standard locations, falling back to: $GOPLS_EXECUTABLE_NAME")
        return GOPLS_EXECUTABLE_NAME
    }

    fun isGoplsInstalled(sdkHomePath: String?): Boolean {
        val path = getGoplsExecutablePath(sdkHomePath)
        if (path != GOPLS_EXECUTABLE_NAME && File(FileUtil.toSystemDependentName(path ?: "")).exists()) return true
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

    fun suggestSdkHomePaths(): List<String> {
        LOG.debug("Suggesting SDK home paths...")
        val suggestions = mutableListOf<String>()
        val home = System.getProperty("user.home")
        val goEnv = GoEnvironmentReader.readGoEnvFile()

        goEnv[GoConstants.Env.GOROOT]?.let {
            LOG.debug("Checking GOROOT from Go env file: $it")
            if (isValidSdkHome(it)) suggestions.add(it)
        }

        GoEnvironmentReader.resolveGoPath().forEach { gopathEntry ->
            scanVersionedDir(File(gopathEntry, "sdk"), prefix = GoConstants.Sdk.VERSION_PREFIX, suggestions)
        }

        System.getenv(GoConstants.Env.GOROOT)?.let {
            LOG.debug("Checking GOROOT: $it")
            if (isValidSdkHome(it)) suggestions.add(it)
        }

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

        scanVersionedDir(File(home, "sdk"), prefix = GoConstants.Sdk.VERSION_PREFIX, suggestions)
        scanVersionedDir(File(home, ".gvm/gos"), prefix = GoConstants.Sdk.VERSION_PREFIX, suggestions)
        scanVersionedDir(File(home, ".goenv/versions"), prefix = "", suggestions)

        val asdfGolangDir = File(home, ".asdf/installs/golang")
        if (asdfGolangDir.isDirectory) {
            asdfGolangDir.listFiles()?.filter { it.isDirectory }?.forEach { versionDir ->
                val sdkHome = File(versionDir, "go")
                val path = if (sdkHome.isDirectory) sdkHome.absolutePath else versionDir.absolutePath
                LOG.debug("Checking asdf golang install: $path")
                if (isValidSdkHome(path)) suggestions.add(path)
            }
        }

        scanVersionedDir(File(home, ".local/share/mise/installs/go"), prefix = "", suggestions)

        if (SystemInfo.isWindows) {
            listOf("C:\\Go", "C:\\Program Files\\Go").forEach {
                LOG.debug("Checking Windows path: $it")
                if (isValidSdkHome(it)) suggestions.add(it)
            }
            val scoopCurrent = File(home, "scoop/apps/go/current")
            LOG.debug("Checking Scoop: ${scoopCurrent.absolutePath}")
            if (isValidSdkHome(scoopCurrent.absolutePath)) suggestions.add(scoopCurrent.absolutePath)
            val chocoPath = "C:\\ProgramData\\chocolatey\\lib\\golang\\tools"
            LOG.debug("Checking Chocolatey: $chocoPath")
            if (isValidSdkHome(chocoPath)) suggestions.add(chocoPath)
        } else {
            listOf("/usr/local/go", "/usr/lib/go").forEach {
                LOG.debug("Checking Unix path: $it")
                if (isValidSdkHome(it)) suggestions.add(it)
            }
            if (SystemInfo.isMac) {
                listOf("/opt/homebrew/Cellar/go", "/usr/local/Cellar/go").forEach { cellar ->
                    scanVersionedDir(File(cellar), prefix = "", suggestions, subDir = "libexec")
                }
            }
            val snapPath = "/snap/go/current"
            LOG.debug("Checking snap: $snapPath")
            if (isValidSdkHome(snapPath)) suggestions.add(snapPath)
        }

        val result = suggestions.map { FileUtil.toSystemIndependentName(it) }.distinct()
        LOG.info("Suggested SDK paths: $result")
        return result
    }

    private fun scanVersionedDir(dir: File, prefix: String, suggestions: MutableList<String>, subDir: String? = null) {
        if (!dir.isDirectory) return
        dir.listFiles()?.filter { it.isDirectory && it.name.startsWith(prefix) }?.forEach { entry ->
            val candidate = if (subDir != null) File(entry, subDir) else entry
            LOG.debug("Checking versioned SDK dir: ${candidate.absolutePath}")
            if (isValidSdkHome(candidate.absolutePath)) suggestions.add(candidate.absolutePath)
        }
    }
}
