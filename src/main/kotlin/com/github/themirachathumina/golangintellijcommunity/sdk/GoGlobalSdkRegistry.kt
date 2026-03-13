package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.messages.Topic

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
        return myState.sdkPaths.filter { path ->
            val version = GoSdkUtils.retrieveGoVersion(path)
            version == null || GoSdkUtils.isSupportedVersion(version)
        }.toSet()
    }

    fun addSdkPath(path: String) {
        if (GoSdkUtils.isValidSdkHome(path)) {
            if (myState.sdkPaths.add(path)) {
                ApplicationManager.getApplication().messageBus.syncPublisher(TOPIC).sdkRegistryChanged()
            }
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
