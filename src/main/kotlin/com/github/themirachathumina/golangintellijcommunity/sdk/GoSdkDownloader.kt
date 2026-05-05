package com.github.themirachathumina.golangintellijcommunity.sdk

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

object GoSdkDownloader {
    private val LOG = Logger.getInstance(GoSdkDownloader::class.java)
    private val proxyLock = Any()
    private val VERSION_TAG_REGEX = Regex("""^go(\d+\.\d+(\.\d+)?)""")

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
            val sdkBaseDir = File(System.getProperty("user.home"), "sdk")
            if (!sdkBaseDir.exists()) sdkBaseDir.mkdirs()

            val archiveFile = File(sdkBaseDir, fileInfo.filename)
            val extractDir = File(sdkBaseDir, "tmp_$version")
            val destDir = File(sdkBaseDir, version)

            if (destDir.exists()) {
                val innerGo = File(destDir, "go")
                val existing = if (innerGo.exists()) innerGo.absolutePath else destDir.absolutePath
                return FileUtil.toSystemIndependentName(existing)
            }

            indicator.text = "Downloading $version..."
            withProxyConfigured { HttpRequests.request(downloadUrl).saveToFile(archiveFile, indicator) }

            indicator.text = "Extracting $version..."
            if (archiveFile.name.endsWith(".zip")) {
                Decompressor.Zip(archiveFile.toPath()).extract(extractDir.toPath())
            } else if (archiveFile.name.endsWith(".tar.gz")) {
                Decompressor.Tar(archiveFile.toPath()).extract(extractDir.toPath())
            }

            archiveFile.delete()

            val extractedGoDir = File(extractDir, "go")
            if (extractedGoDir.exists()) {
                extractedGoDir.renameTo(destDir)
                extractDir.deleteRecursively()
            } else {
                extractDir.renameTo(destDir)
            }

            val finalPath = FileUtil.toSystemIndependentName(destDir.absolutePath)
            GoGlobalSdkRegistry.getInstance().addSdkPath(finalPath)
            return finalPath
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
            ?: System.getenv("HTTPS_PROXY")
            ?: System.getenv("https_proxy")
            ?: System.getenv("HTTP_PROXY")
            ?: System.getenv("http_proxy")
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
