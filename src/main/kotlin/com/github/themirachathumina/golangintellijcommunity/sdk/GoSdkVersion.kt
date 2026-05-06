package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.util.FileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.ProcessRunner
import com.github.themirachathumina.golangintellijcommunity.util.RealFileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.RealProcessRunner
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.ConcurrentHashMap

/*
 * Provides utilities for retrieving and comparing Go SDK versions. 
 * Supports reading versions from VERSION files, executing 'go version', and 
 * caching results for improved performance.
 */
class GoSdkVersion(
    private val fs: FileSystemAccess = RealFileSystemAccess(),
    private val runner: ProcessRunner = RealProcessRunner()
) {
    companion object {
        private val LOG = Logger.getInstance(GoSdkVersion::class.java)

        private val VERSION_FILE_REGEX = """^go(\d+\.\d+(\.\d+)?)""".toRegex()
        private val VERSION_OUTPUT_REGEX = """go version go(\d+\.\d+(\.\d+)?)""".toRegex()

        private val versionCache = ConcurrentHashMap<String, String>()
        private val pendingVersions = ConcurrentHashMap.newKeySet<String>()

        private val DEFAULT = GoSdkVersion()

        fun retrieveGoVersion(sdkHomePath: String): String? = DEFAULT.retrieveGoVersion(sdkHomePath)
        fun compareVersions(v1: String, v2: String): Int = DEFAULT.compareVersions(v1, v2)
        fun isSupportedVersion(version: String): Boolean = DEFAULT.isSupportedVersion(version)
    }

    fun retrieveGoVersion(sdkHomePath: String): String? {
        versionCache[sdkHomePath]?.let { return it }

        val versionFromFile = readVersionFile(sdkHomePath)
        if (versionFromFile != null) {
            LOG.debug("Read Go version $versionFromFile from VERSION file for $sdkHomePath")
            versionCache[sdkHomePath] = versionFromFile
            return versionFromFile
        }

        val exePath = GoSdkLocator.getGoExecutablePath(sdkHomePath)
        if (exePath == null) return null

        if (ApplicationManager.getApplication().isDispatchThread) {
            if (!pendingVersions.contains(sdkHomePath)) {
                pendingVersions.add(sdkHomePath)
                LOG.info("${GoConstants.Log.PREFIX} Scheduling background version retrieval for: $sdkHomePath")
                ApplicationManager.getApplication().executeOnPooledThread {
                    try {
                        doRetrieveGoVersion(sdkHomePath, exePath)
                    } finally {
                        pendingVersions.remove(sdkHomePath)
                        ApplicationManager.getApplication().invokeLater({
                            ApplicationManager.getApplication().messageBus.syncPublisher(GoGlobalSdkRegistry.TOPIC).sdkRegistryChanged()
                        }, ModalityState.any())
                    }
                }
            }
            return null
        }

        return doRetrieveGoVersion(sdkHomePath, exePath)
    }

    private fun readVersionFile(sdkHomePath: String): String? {
        val versionFile = fs.combine(sdkHomePath, GoConstants.Sdk.VERSION_FILE)
        if (!fs.exists(versionFile)) return null
        return try {
            val content = fs.readText(versionFile)?.trim() ?: return null
            VERSION_FILE_REGEX.find(content)?.groupValues?.get(1)
        } catch (e: Exception) {
            LOG.debug("Failed to read VERSION file at $sdkHomePath: ${e.message}")
            null
        }
    }

    private fun doRetrieveGoVersion(sdkHomePath: String, exePath: String): String? {
        try {
            LOG.debug("Running '$exePath version' to retrieve Go version...")
            val output = runner.run(listOf(exePath, "version"))

            if (output.exitCode == 0) {
                val stdout = output.stdout
                val match = VERSION_OUTPUT_REGEX.find(stdout)
                val version = match?.groupValues?.get(1)
                if (version != null) {
                    LOG.info("Detected Go version $version for $sdkHomePath")
                    versionCache[sdkHomePath] = version
                    return version
                }
            }
        } catch (e: Exception) {
            LOG.error("Error retrieving Go version for $sdkHomePath", e)
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
