package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.util.FileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.ProcessRunner
import com.github.themirachathumina.golangintellijcommunity.util.RealFileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.RealProcessRunner
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import java.io.File

/*
 * Locates Go and gopls executables on the system. Scans standard installation paths, 
 * environment variables, and project-specific SDK locations to find valid Go environments.
 */
class GoSdkLocator(
    private val fs: FileSystemAccess = RealFileSystemAccess(),
    private val runner: ProcessRunner = RealProcessRunner()
) {
    companion object {
        private val LOG = Logger.getInstance(GoSdkLocator::class.java)

        val GO_EXECUTABLE_NAME = if (SystemInfo.isWindows) "go.exe" else GoConstants.Executables.GO
        val GOPLS_EXECUTABLE_NAME = if (SystemInfo.isWindows) "gopls.exe" else GoConstants.Executables.GOPLS

        private val DEFAULT = GoSdkLocator()

        fun getGoExecutablePath(sdkHomePath: String) = DEFAULT.getGoExecutablePath(sdkHomePath)
        fun getGoplsExecutablePath(sdkHomePath: String?) = DEFAULT.getGoplsExecutablePath(sdkHomePath)
        fun isGoplsInstalled(sdkHomePath: String?) = DEFAULT.isGoplsInstalled(sdkHomePath)
        fun isValidSdkHome(path: String) = DEFAULT.isValidSdkHome(path)
        fun suggestSdkHomePaths() = DEFAULT.suggestSdkHomePaths()
    }

    fun getGoExecutablePath(sdkHomePath: String): String? {
        val goExe = fs.combine(sdkHomePath, "bin", GO_EXECUTABLE_NAME)
        val exists = fs.exists(goExe) && fs.canExecute(goExe)
        if (!exists) {
            LOG.debug("${GoConstants.Log.PREFIX} Go executable not found or not executable at: $goExe")
        }
        return if (exists) goExe else null
    }

    fun getGoplsExecutablePath(sdkHomePath: String?): String? {
        if (sdkHomePath != null) {
            val goplsExe = fs.combine(sdkHomePath, "bin", GOPLS_EXECUTABLE_NAME)
            if (fs.exists(goplsExe) && fs.canExecute(goplsExe)) {
                LOG.debug("Found gopls in SDK bin: $goplsExe")
                return goplsExe
            }
        }

        val gopathEntries = buildList {
            val goEnv = GoEnvironmentReader.readGoEnvFile()
            goEnv[GoConstants.Env.GOPATH]?.split(File.pathSeparator)?.forEach { add(it) }
            fs.getEnv(GoConstants.Env.GOPATH)?.split(File.pathSeparator)?.forEach { add(it) }
            add(fs.combine(fs.getUserHome(), "go"))
        }.distinct()

        for (gopathEntry in gopathEntries) {
            val gopathBin = fs.combine(gopathEntry, "bin", GOPLS_EXECUTABLE_NAME)
            if (fs.exists(gopathBin) && fs.canExecute(gopathBin)) {
                LOG.debug("Found gopls in GOPATH bin: $gopathBin")
                return gopathBin
            }
        }

        LOG.debug("gopls not found in standard locations, falling back to: $GOPLS_EXECUTABLE_NAME")
        return GOPLS_EXECUTABLE_NAME
    }

    fun isGoplsInstalled(sdkHomePath: String?): Boolean {
        val path = getGoplsExecutablePath(sdkHomePath)
        if (path != GOPLS_EXECUTABLE_NAME && fs.exists(path ?: "")) return true
        return try {
            val output = runner.run(listOf(GOPLS_EXECUTABLE_NAME, "version"), timeoutMs = 2000)
            output.exitCode == 0
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
        val home = fs.getUserHome()
        val goEnv = GoEnvironmentReader.readGoEnvFile()

        goEnv[GoConstants.Env.GOROOT]?.let {
            LOG.debug("Checking GOROOT from Go env file: $it")
            if (isValidSdkHome(it)) suggestions.add(it)
        }

        GoEnvironmentReader.resolveGoPath().forEach { gopathEntry ->
            scanVersionedDir(fs.combine(gopathEntry, "sdk"), prefix = GoConstants.Sdk.VERSION_PREFIX, suggestions)
        }

        fs.getEnv(GoConstants.Env.GOROOT)?.let {
            LOG.debug("Checking GOROOT: $it")
            if (isValidSdkHome(it)) suggestions.add(it)
        }

        fs.getEnv("PATH")?.split(File.pathSeparator)?.forEach { pathEntry ->
            if (fs.isDirectory(pathEntry) &&
                fs.getName(pathEntry).equals("bin", ignoreCase = true) &&
                pathEntry.contains("go", ignoreCase = true)) {
                val parentPath = fs.getParent(pathEntry)
                if (parentPath != null && isValidSdkHome(parentPath)) {
                    LOG.debug("Found valid home from PATH entry $pathEntry: $parentPath")
                    suggestions.add(parentPath)
                }
            }
        }

        scanVersionedDir(fs.combine(home, "sdk"), prefix = GoConstants.Sdk.VERSION_PREFIX, suggestions)
        scanVersionedDir(fs.combine(home, ".gvm/gos"), prefix = GoConstants.Sdk.VERSION_PREFIX, suggestions)
        scanVersionedDir(fs.combine(home, ".goenv/versions"), prefix = "", suggestions)

        val asdfGolangDir = fs.combine(home, ".asdf/installs/golang")
        if (fs.isDirectory(asdfGolangDir)) {
            fs.listChildren(asdfGolangDir).forEach { versionDir ->
                if (fs.isDirectory(versionDir)) {
                    val sdkHome = fs.combine(versionDir, "go")
                    val path = if (fs.isDirectory(sdkHome)) sdkHome else versionDir
                    LOG.debug("Checking asdf golang install: $path")
                    if (isValidSdkHome(path)) suggestions.add(path)
                }
            }
        }

        scanVersionedDir(fs.combine(home, ".local/share/mise/installs/go"), prefix = "", suggestions)

        if (SystemInfo.isWindows) {
            listOf("C:\\Go", "C:\\Program Files\\Go").forEach {
                LOG.debug("Checking Windows path: $it")
                if (isValidSdkHome(it)) suggestions.add(it)
            }
            val scoopCurrent = fs.combine(home, "scoop/apps/go/current")
            LOG.debug("Checking Scoop: $scoopCurrent")
            if (isValidSdkHome(scoopCurrent)) suggestions.add(scoopCurrent)
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
                    scanVersionedDir(cellar, prefix = "", suggestions, subDir = "libexec")
                }
            }
            val snapPath = "/snap/go/current"
            LOG.debug("Checking snap: $snapPath")
            if (isValidSdkHome(snapPath)) suggestions.add(snapPath)
        }

        val result = suggestions.distinct()
        LOG.info("Suggested SDK paths: $result")
        return result
    }

    private fun scanVersionedDir(dir: String, prefix: String, suggestions: MutableList<String>, subDir: String? = null) {
        if (!fs.isDirectory(dir)) return
        fs.listChildren(dir).filter { fs.isDirectory(it) && fs.getName(it).startsWith(prefix) }.forEach { entry ->
            val candidate = if (subDir != null) fs.combine(entry, subDir) else entry
            LOG.debug("Checking versioned SDK dir: $candidate")
            if (isValidSdkHome(candidate)) suggestions.add(candidate)
        }
    }
}
