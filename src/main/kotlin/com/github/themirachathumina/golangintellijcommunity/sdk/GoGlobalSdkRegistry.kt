package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.messages.Topic

interface GoGlobalSdkRegistryListener {
    fun sdkRegistryChanged()
}

/*
 * Application-level registry that tracks all detected and manually added Go SDKs.
 * Persists the list of SDK paths and provides a central point for SDK discovery 
 * and version management across all projects.
 */
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
        val invalid = myState.sdkPaths.filterNot { GoSdkLocator.isValidSdkHome(it) }
        if (invalid.isNotEmpty()) {
            LOG.info("${GoConstants.Log.PREFIX} Removing invalid SDK paths: $invalid")
            myState.sdkPaths.removeAll(invalid.toSet())
        }
        val paths = myState.sdkPaths.filter { path ->
            val version = GoSdkVersion.retrieveGoVersion(path)
            version == null || GoSdkVersion.isSupportedVersion(version)
        }.toSet()
        if (myState.cachedGoOS.isEmpty() && paths.isNotEmpty()) {
            refreshEnvCache(paths.first())
        }
        return paths
    }

    fun addSdkPath(path: String) {
        val normalized = FileUtil.toSystemIndependentName(path)
        if (GoSdkLocator.isValidSdkHome(normalized)) {
            if (myState.sdkPaths.add(normalized)) {
                refreshEnvCache(normalized)
                ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC).sdkRegistryChanged()
            }
        }
    }

    fun refreshEnvCache(sdkPath: String) {
        if (myState.cachedGoOS.isNotEmpty()) return
        val exePath = GoSdkLocator.getGoExecutablePath(sdkPath) ?: return
        val env = GoEnvironmentReader.fetchGoEnv(exePath, GoConstants.Env.GOOS, GoConstants.Env.GOARCH, GoConstants.Env.GOPROXY)
        val goos = env[GoConstants.Env.GOOS]
        val goarch = env[GoConstants.Env.GOARCH]
        val goproxy = env[GoConstants.Env.GOPROXY]
        if (goos != null && goarch != null && goproxy != null) {
            myState.cachedGoOS = goos
            myState.cachedGoArch = goarch
            myState.cachedGoProxy = goproxy
        }
    }

    private fun ensureScanned() {
        if (!myState.hasScannedInitial) {
            LOG.info("${GoConstants.Log.PREFIX} First time registry access, scanning standard locations...")
            myState.hasScannedInitial = true

            ApplicationManager.getApplication().executeOnPooledThread {
                val suggested = GoSdkLocator.suggestSdkHomePaths()
                suggested.forEach { path ->
                    GoSdkVersion.retrieveGoVersion(path)
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
