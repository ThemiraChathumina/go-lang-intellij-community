package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import java.io.File

object GoEnvironmentReader {
    private val LOG = Logger.getInstance(GoEnvironmentReader::class.java)

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

    fun resolveGoPath(): List<String> {
        val goEnv = readGoEnvFile()
        return buildList {
            goEnv[GoConstants.Env.GOPATH]?.split(File.pathSeparator)?.forEach { add(FileUtil.toSystemIndependentName(it)) }
            System.getenv(GoConstants.Env.GOPATH)?.split(File.pathSeparator)?.forEach { add(FileUtil.toSystemIndependentName(it)) }
            add(FileUtil.toSystemIndependentName(File(System.getProperty("user.home"), "go").absolutePath))
        }.distinct()
    }

    fun resolveGoModCache(): String {
        val goEnv = readGoEnvFile()
        goEnv[GoConstants.Env.GOMODCACHE]?.let { return FileUtil.toSystemIndependentName(it) }
        System.getenv(GoConstants.Env.GOMODCACHE)?.let { return FileUtil.toSystemIndependentName(it) }
        return FileUtil.toSystemIndependentName(File(resolveGoPath().first(), "pkg/mod").absolutePath)
    }

    fun fetchGoEnv(goExePath: String, vararg vars: String): Map<String, String> {
        try {
            val commandLine = GeneralCommandLine(FileUtil.toSystemDependentName(goExePath), "env").apply {
                addParameters(*vars)
            }
            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess(3000)
            if (output.exitCode == 0) {
                val lines = output.stdoutLines
                return vars.zip(lines).toMap()
            }
        } catch (e: Exception) {
            LOG.debug("Failed to fetch go env: ${e.message}")
        }
        return emptyMap()
    }
}
