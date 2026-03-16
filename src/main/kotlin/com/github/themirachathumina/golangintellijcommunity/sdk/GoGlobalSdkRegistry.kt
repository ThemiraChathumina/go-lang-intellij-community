package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.messages.Topic
import java.util.concurrent.TimeUnit

interface GoGlobalSdkRegistryListener {
    fun sdkRegistryChanged()
}

@Service(Service.Level.APP)
@State(
    name = "GoGlobalSdkRegistry",
    storages = [Storage("GoLangCommunityGlobalSdk.xml")]
)
class GoGlobalSdkRegistry : PersistentStateComponent<GoGlobalSdkRegistry.State> {
    private val LOG = Logger.getInstance(GoGlobalSdkRegistry::class.java)

    class State {
        var sdkPaths: MutableSet<String> = mutableSetOf()
        var hasScannedInitial: Boolean = false
        var sdkDownloadBaseUrl: String = DEFAULT_DOWNLOAD_URL
        var cachedGoOS: String = ""
        var cachedGoArch: String = ""
        var cachedGoProxy: String = ""
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun getSdkPaths(): Set<String> {
        ensureScanned()
        val invalid = myState.sdkPaths.filterNot { GoSdkUtils.isValidSdkHome(it) }
        if (invalid.isNotEmpty()) {
            LOG.info("[Go-Lang-Community] Removing invalid SDK paths: $invalid")
            myState.sdkPaths.removeAll(invalid.toSet())
        }
        val paths = myState.sdkPaths.filter { path ->
            val version = GoSdkUtils.retrieveGoVersion(path)
            version == null || GoSdkUtils.isSupportedVersion(version)
        }.toSet()
        // Populate env cache from any valid SDK if not yet cached
        if (myState.cachedGoOS.isEmpty() && paths.isNotEmpty()) {
            refreshEnvCache(paths.first())
        }
        return paths
    }

    fun addSdkPath(path: String) {
        val normalized = FileUtil.toSystemIndependentName(path)
        if (GoSdkUtils.isValidSdkHome(normalized)) {
            if (myState.sdkPaths.add(normalized)) {
                refreshEnvCache(normalized)
                ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC).sdkRegistryChanged()
            }
        }
    }

    /**
     * Caches GOOS, GOARCH, GOPROXY by running `go env` via ProcessBuilder.
     * Safe to call from any thread. Only updates if cache is currently empty.
     */
    fun refreshEnvCache(sdkPath: String) {
        if (myState.cachedGoOS.isNotEmpty()) return
        val exePath = GoSdkUtils.getGoExecutablePath(sdkPath) ?: return
        try {
            val process = ProcessBuilder(FileUtil.toSystemDependentName(exePath), "env", "GOOS", "GOARCH", "GOPROXY").start()
            val output = process.inputStream.bufferedReader().readText()
            if (process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0) {
                val lines = output.trim().lines()
                if (lines.size >= 3) {
                    myState.cachedGoOS = lines[0]
                    myState.cachedGoArch = lines[1]
                    myState.cachedGoProxy = lines[2]
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun ensureScanned() {
        if (!myState.hasScannedInitial) {
            LOG.info("[Go-Lang-Community] First time registry access, scanning standard locations...")
            myState.hasScannedInitial = true
            
            ApplicationManager.getApplication().executeOnPooledThread {
                val suggested = GoSdkUtils.suggestSdkHomePaths()
                suggested.forEach { path ->
                    // Prime the version cache in background
                    GoSdkUtils.retrieveGoVersion(path)
                    addSdkPath(path)
                }
            }
        }
    }

    fun getDownloadBaseUrl(): String = myState.sdkDownloadBaseUrl.ifBlank { DEFAULT_DOWNLOAD_URL }

    fun setDownloadBaseUrl(url: String) {
        myState.sdkDownloadBaseUrl = url.trimEnd('/')
    }

    companion object {
        const val DEFAULT_DOWNLOAD_URL = "https://go.dev/dl"
        val TOPIC = Topic.create("Go Global SDK Registry Changes", GoGlobalSdkRegistryListener::class.java)
        fun getInstance(): GoGlobalSdkRegistry = ApplicationManager.getApplication().getService(GoGlobalSdkRegistry::class.java)
    }
}
