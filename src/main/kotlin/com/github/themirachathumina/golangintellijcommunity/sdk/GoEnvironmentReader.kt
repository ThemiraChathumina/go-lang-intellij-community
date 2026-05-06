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
 * Reads Go environment settings from the system and Go environment files.
 * Resolves GOPATH, GOMODCACHE, and GOROOT by checking environment variables,
 * standard locations, and executing 'go env' commands.
 */
class GoEnvironmentReader(
    private val fs: FileSystemAccess = RealFileSystemAccess(),
    private val runner: ProcessRunner = RealProcessRunner()
) {
    companion object {
        private val LOG = Logger.getInstance(GoEnvironmentReader::class.java)
        private val DEFAULT = GoEnvironmentReader()

        fun readGoEnvFile() = DEFAULT.readGoEnvFile()
        fun resolveGoPath() = DEFAULT.resolveGoPath()
        fun resolveGoModCache() = DEFAULT.resolveGoModCache()
        fun fetchGoEnv(goExePath: String, vararg vars: String) = DEFAULT.fetchGoEnv(goExePath, *vars)
    }

    fun readGoEnvFile(): Map<String, String> {
        val goenvVar = fs.getEnv("GOENV")
        if (goenvVar == "off") return emptyMap()

        val envFile = when {
            goenvVar != null -> goenvVar
            SystemInfo.isWindows -> fs.combine(fs.getEnv("AppData") ?: return emptyMap(), "go/env")
            else -> {
                val xdgConfig = fs.getEnv("XDG_CONFIG_HOME")
                if (xdgConfig != null) fs.combine(xdgConfig, "go/env")
                else fs.combine(fs.getUserHome(), ".config/go/env")
            }
        }

        if (!fs.exists(envFile)) return emptyMap()
        return try {
            fs.readText(envFile)?.lines()
                ?.filter { it.isNotBlank() && !it.startsWith('#') && '=' in it }
                ?.associate { line ->
                    val idx = line.indexOf('=')
                    line.substring(0, idx).trim() to line.substring(idx + 1).trim()
                } ?: emptyMap()
        } catch (e: Exception) {
            LOG.debug("Failed to read Go env file at $envFile: ${e.message}")
            emptyMap()
        }
    }

    fun resolveGoPath(): List<String> {
        val goEnv = readGoEnvFile()
        return buildList {
            goEnv[GoConstants.Env.GOPATH]?.split(File.pathSeparator)?.forEach { add(it) }
            fs.getEnv(GoConstants.Env.GOPATH)?.split(File.pathSeparator)?.forEach { add(it) }
            add(fs.combine(fs.getUserHome(), "go"))
        }.distinct()
    }

    fun resolveGoModCache(): String {
        val goEnv = readGoEnvFile()
        goEnv[GoConstants.Env.GOMODCACHE]?.let { return it }
        fs.getEnv(GoConstants.Env.GOMODCACHE)?.let { return it }
        return fs.combine(resolveGoPath().first(), "pkg/mod")
    }

    fun fetchGoEnv(goExePath: String, vararg vars: String): Map<String, String> {
        try {
            val command = mutableListOf(goExePath, "env")
            command.addAll(vars)
            val output = runner.run(command)
            if (output.exitCode == 0) {
                val lines = output.stdout.lines().filter { it.isNotBlank() }
                return vars.zip(lines).toMap()
            }
        } catch (e: Exception) {
            LOG.debug("Failed to fetch go env: ${e.message}")
        }
        return emptyMap()
    }
}
