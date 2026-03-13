package com.github.themirachathumina.golangintellijcommunity.sdk

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.Decompressor
import com.intellij.util.io.HttpRequests
import java.io.File

object GoSdkDownloader {
    private val LOG = Logger.getInstance(GoSdkDownloader::class.java)

    data class GoRelease(val version: String, val stable: Boolean, val files: List<GoFile>)
    data class GoFile(val filename: String, val os: String, val arch: String, val kind: String)

    fun fetchVersions(): List<String> {
        try {
            val json = HttpRequests.request("https://go.dev/dl/?mode=json&include=all").readString(null)
            val type = object : TypeToken<List<GoRelease>>() {}.type
            val releases: List<GoRelease> = Gson().fromJson(json, type)
            return releases
                .filter { release ->
                    val version = Regex("""^go(\d+\.\d+(\.\d+)?)""").find(release.version)?.groupValues?.get(1)
                    version != null && GoSdkUtils.isSupportedVersion(version)
                }
                .map { it.version }
        } catch (e: Exception) {
            LOG.error("Failed to fetch Go versions", e)
            return emptyList()
        }
    }

    fun downloadSdk(version: String, indicator: ProgressIndicator): String? {
        try {
            indicator.text = "Fetching download links for $version..."
            val json = HttpRequests.request("https://go.dev/dl/?mode=json&include=all").readString(null)
            val type = object : TypeToken<List<GoRelease>>() {}.type
            val releases: List<GoRelease> = Gson().fromJson(json, type)
            
            val release = releases.find { it.version == version } ?: return null
            
            val targetOs = if (SystemInfo.isWindows) "windows" else if (SystemInfo.isMac) "darwin" else "linux"
            val targetArch = if (SystemInfo.isAarch64) "arm64" else "amd64"
            val targetKind = "archive"

            val fileInfo = release.files.find { it.os == targetOs && it.arch == targetArch && it.kind == targetKind } ?: return null
            
            val downloadUrl = "https://go.dev/dl/${fileInfo.filename}"
            val sdkBaseDir = File(System.getProperty("user.home"), "sdk")
            if (!sdkBaseDir.exists()) sdkBaseDir.mkdirs()
            
            val archiveFile = File(sdkBaseDir, fileInfo.filename)
            val extractDir = File(sdkBaseDir, "tmp_$version")
            val destDir = File(sdkBaseDir, version)

            if (destDir.exists()) {
                val innerGo = File(destDir, "go")
                return if (innerGo.exists()) innerGo.absolutePath else destDir.absolutePath
            }

            indicator.text = "Downloading $version..."
            HttpRequests.request(downloadUrl).saveToFile(archiveFile, indicator)

            indicator.text = "Extracting $version..."
            if (archiveFile.name.endsWith(".zip")) {
                Decompressor.Zip(archiveFile).extract(extractDir)
            } else if (archiveFile.name.endsWith(".tar.gz")) {
                Decompressor.Tar(archiveFile).extract(extractDir)
            }

            archiveFile.delete()

            // The archive extracts to a folder named 'go'. We move it to 'version' dir.
            val extractedGoDir = File(extractDir, "go")
            if (extractedGoDir.exists()) {
                extractedGoDir.renameTo(destDir)
                extractDir.deleteRecursively()
            } else {
                extractDir.renameTo(destDir)
            }

            val finalPath = destDir.absolutePath
            GoGlobalSdkRegistry.getInstance().addSdkPath(finalPath)
            
            return finalPath
        } catch (e: Exception) {
            LOG.error("Failed to download Go SDK", e)
            return null
        }
    }
}
