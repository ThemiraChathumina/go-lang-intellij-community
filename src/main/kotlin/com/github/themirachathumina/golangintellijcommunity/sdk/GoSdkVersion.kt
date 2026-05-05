package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import java.io.File

object GoSdkVersion {
    private val LOG = Logger.getInstance(GoSdkVersion::class.java)

    private val VERSION_FILE_REGEX = """^go(\d+\.\d+(\.\d+)?)""".toRegex()
    private val VERSION_OUTPUT_REGEX = """go version go(\d+\.\d+(\.\d+)?)""".toRegex()

    private val versionCache = mutableMapOf<String, String>()
    private val pendingVersions = mutableSetOf<String>()

    fun retrieveGoVersion(sdkHomePath: String): String? {
        val normalizedPath = FileUtil.toSystemIndependentName(sdkHomePath)
        versionCache[normalizedPath]?.let { return it }

        val versionFromFile = readVersionFile(sdkHomePath)
        if (versionFromFile != null) {
            LOG.debug("Read Go version $versionFromFile from VERSION file for $normalizedPath")
            versionCache[normalizedPath] = versionFromFile
            return versionFromFile
        }

        val exePath = GoSdkLocator.getGoExecutablePath(sdkHomePath)
        if (exePath == null) return null

        if (ApplicationManager.getApplication().isDispatchThread) {
            if (!pendingVersions.contains(normalizedPath)) {
                pendingVersions.add(normalizedPath)
                LOG.info("${GoConstants.Log.PREFIX} Scheduling background version retrieval for: $normalizedPath")
                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        doRetrieveGoVersion(normalizedPath, exePath)
                    } finally {
                        pendingVersions.remove(normalizedPath)
                        ApplicationManager.getApplication().invokeLater({
                            ApplicationManager.getApplication().messageBus.syncPublisher(GoGlobalSdkRegistry.TOPIC).sdkRegistryChanged()
                        }, ModalityState.any())
                    }
                }
            }
            return null
        }

        return doRetrieveGoVersion(normalizedPath, exePath)
    }

    private fun readVersionFile(sdkHomePath: String): String? {
        val versionFile = File(FileUtil.toSystemDependentName(sdkHomePath), GoConstants.Sdk.VERSION_FILE)
        if (!versionFile.exists()) return null
        return try {
            val content = versionFile.readText().trim()
            VERSION_FILE_REGEX.find(content)?.groupValues?.get(1)
        } catch (e: Exception) {
            LOG.debug("Failed to read VERSION file at $sdkHomePath: ${e.message}")
            null
        }
    }

    private fun doRetrieveGoVersion(normalizedPath: String, exePath: String): String? {
        try {
            val sysDepExe = FileUtil.toSystemDependentName(exePath)
            LOG.debug("Running '$sysDepExe version' to retrieve Go version...")
            val commandLine = GeneralCommandLine(sysDepExe, "version")
            val handler = CapturingProcessHandler(commandLine)
            val output = handler.runProcess(5000)

            if (output.exitCode == 0) {
                val stdout = output.stdout
                val match = VERSION_OUTPUT_REGEX.find(stdout)
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

    fun isSupportedVersion(version: String): Boolean =
        compareVersions(version, GoConstants.Sdk.MIN_VERSION) >= 0
}
