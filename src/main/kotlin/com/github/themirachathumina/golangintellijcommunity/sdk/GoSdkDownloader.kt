package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.util.FileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.RealFileSystemAccess
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.io.Decompressor
import com.intellij.util.io.HttpRequests
import java.io.File
import java.net.URI

/*
 * Handles the discovery and downloading of Go SDKs from the official Go release server.
 * Fetches available versions, identifies the correct archive for the current platform, 
 * and performs the download and extraction process.
 */
class GoSdkDownloader(
    private val fs: FileSystemAccess = RealFileSystemAccess()
) {
    companion object {
        private val LOG = Logger.getInstance(GoSdkDownloader::class.java)
        private val proxyLock = Any()
        private val VERSION_TAG_REGEX = Regex("""^go(\d+\.\d+(\.\d+)?)""")

        private val DEFAULT = GoSdkDownloader()

        fun fetchVersions(): List<String> = DEFAULT.fetchVersions()
        fun downloadSdk(version: String, indicator: ProgressIndicator): String? = DEFAULT.downloadSdk(version, indicator)
    }

    data class GoRelease(val version: String, val stable: Boolean, val files: List<GoFile>)
    data class GoFile(val filename: String, val os: String, val arch: String, val kind: String)

    fun fetchVersions(): List<String> {
        return try {
            val baseUrl = GoGlobalSdkRegistry.getInstance().getDownloadBaseUrl()
            val json = withProxyConfigured { HttpRequests.request("$baseUrl/?mode=json&include=all").readString(null) }
            val type = object : TypeToken<List<GoRelease>>() {}.type
            val releases: List<GoRelease> = Gson().fromJson(json, type)
            releases
                .filter { release ->
                    val version = VERSION_TAG_REGEX.find(release.version)?.groupValues?.get(1)
                    version != null && GoSdkVersion.isSupportedVersion(version)
                }
                .map { it.version }
        } catch (e: Exception) {
            LOG.error("Failed to fetch Go versions", e)
            emptyList()
        }
    }

    fun downloadSdk(version: String, indicator: ProgressIndicator): String? {
        try {
            val baseUrl = GoGlobalSdkRegistry.getInstance().getDownloadBaseUrl()

            indicator.text = "Fetching download links for $version..."
            val json = withProxyConfigured { HttpRequests.request("$baseUrl/?mode=json&include=all").readString(null) }
            val type = object : TypeToken<List<GoRelease>>() {}.type
            val releases: List<GoRelease> = Gson().fromJson(json, type)

            val release = releases.find { it.version == version } ?: return null

            val targetOs = if (SystemInfo.isWindows) "windows" else if (SystemInfo.isMac) "darwin" else "linux"
            val targetArch = if (SystemInfo.isAarch64) "arm64" else "amd64"

            val fileInfo = release.files.find { it.os == targetOs && it.arch == targetArch && it.kind == "archive" } ?: return null

            val downloadUrl = "$baseUrl/${fileInfo.filename}"
            val sdkBaseDir = fs.combine(fs.getUserHome(), "sdk")
            if (!fs.exists(sdkBaseDir)) fs.mkdirs(sdkBaseDir)

            val archivePath = fs.combine(sdkBaseDir, fileInfo.filename)
            val archiveFile = File(FileUtil.toSystemDependentName(archivePath))
            val extractPath = fs.combine(sdkBaseDir, "tmp_$version")
            val destPath = fs.combine(sdkBaseDir, version)

            if (fs.exists(destPath)) {
                val innerGo = fs.combine(destPath, "go")
                val existing = if (fs.exists(innerGo)) innerGo else destPath
                return existing
            }

            indicator.text = "Downloading $version..."
            withProxyConfigured { HttpRequests.request(downloadUrl).saveToFile(archiveFile, indicator) }

            indicator.text = "Extracting $version..."
            if (archivePath.endsWith(".zip")) {
                Decompressor.Zip(archiveFile.toPath()).extract(File(FileUtil.toSystemDependentName(extractPath)).toPath())
            } else if (archivePath.endsWith(".tar.gz")) {
                Decompressor.Tar(archiveFile.toPath()).extract(File(FileUtil.toSystemDependentName(extractPath)).toPath())
            }

            fs.delete(archivePath)

            val extractedGoDir = fs.combine(extractPath, "go")
            if (fs.exists(extractedGoDir)) {
                fs.renameTo(extractedGoDir, destPath)
                fs.deleteRecursively(extractPath)
            } else {
                fs.renameTo(extractPath, destPath)
            }

            GoGlobalSdkRegistry.getInstance().addSdkPath(destPath)
            return destPath
        } catch (e: Exception) {
            LOG.error("Failed to download Go SDK", e)
            return null
        }
    }

    private fun <T> withProxyConfigured(block: () -> T): T {
        val proxyUrl = resolveProxyUrl() ?: return block()

        val parsed = parseProxyUrl(proxyUrl) ?: run {
            LOG.debug("Could not parse proxy URL: $proxyUrl")
            return block()
        }
        val (host, port, scheme) = parsed
        val prefix = if (scheme == "http") "http" else "https"

        synchronized(proxyLock) {
            val prevHost = System.getProperty("$prefix.proxyHost")
            val prevPort = System.getProperty("$prefix.proxyPort")
            try {
                System.setProperty("$prefix.proxyHost", host)
                System.setProperty("$prefix.proxyPort", port.toString())
                LOG.debug("Using proxy $host:$port for SDK download")
                return block()
            } finally {
                if (prevHost != null) System.setProperty("$prefix.proxyHost", prevHost)
                else System.clearProperty("$prefix.proxyHost")
                if (prevPort != null) System.setProperty("$prefix.proxyPort", prevPort)
                else System.clearProperty("$prefix.proxyPort")
            }
        }
    }

    private fun resolveProxyUrl(): String? {
        val goEnv = GoEnvironmentReader.readGoEnvFile()
        return goEnv["HTTPS_PROXY"]
            ?: goEnv["https_proxy"]
            ?: goEnv["HTTP_PROXY"]
            ?: goEnv["http_proxy"]
            ?: fs.getEnv("HTTPS_PROXY")
            ?: fs.getEnv("https_proxy")
            ?: fs.getEnv("HTTP_PROXY")
            ?: fs.getEnv("http_proxy")
    }

    private fun parseProxyUrl(proxyUrl: String): Triple<String, Int, String>? {
        return try {
            val url = URI(if (proxyUrl.contains("://")) proxyUrl else "http://$proxyUrl").toURL()
            val host = url.host.takeIf { it.isNotBlank() } ?: return null
            val port = if (url.port == -1) 8080 else url.port
            val scheme = url.protocol ?: "http"
            Triple(host, port, scheme)
        } catch (e: Exception) {
            null
        }
    }
}
