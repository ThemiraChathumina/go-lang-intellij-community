package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.projectRoots.*
import org.jdom.Element
import javax.swing.Icon

class GoSdkType : SdkType("Go SDK") {
    private val LOG = Logger.getInstance(GoSdkType::class.java)

    companion object {
        fun getInstance(): GoSdkType = findInstance(GoSdkType::class.java)
    }

    override fun suggestHomePath(): String? {
        val suggested = GoSdkUtils.suggestSdkHomePaths().firstOrNull()
        LOG.debug("[Go-Lang-Community] GoSdkType: Suggested home path: $suggested")
        return suggested
    }

    override fun isValidSdkHome(path: String): Boolean {
        val isValid = GoSdkUtils.isValidSdkHome(path)
        LOG.debug("GoSdkType: Is valid SDK home ($path): $isValid")
        return isValid
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String): String {
        val version = getVersionString(sdkHome)
        val name = if (version != null) "Go $version" else "Go SDK"
        LOG.debug("GoSdkType: Suggested SDK name for $sdkHome: $name")
        return name
    }

    override fun getVersionString(sdkHome: String): String? {
        println("getVersionString: $sdkHome")
        return GoSdkUtils.retrieveGoVersion(sdkHome)
    }

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? {
        return null
    }

    override fun getPresentableName(): String = "Go SDK"

    override fun getIcon(): Icon = GoIcons.FILE

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {}

    override fun setupSdkPaths(sdk: Sdk, sdkModel: SdkModel): Boolean {
        val modificator = sdk.sdkModificator
        val homePath = sdk.homePath ?: return false
        val version = getVersionString(homePath)
        println("GoSdkType: Setting up SDK paths for $homePath (version: $version)")
        LOG.info("GoSdkType: Setting up SDK paths for $homePath (version: $version)")
        modificator.versionString = version

        // --- THE FIX: Explicitly set the name here ---
        if (version != null) {
            val name = "Go $version"
            LOG.info("GoSdkType: Renaming SDK to $name")
            modificator.name = name
        }

        // No need to commit here, the framework handles it
        return true
    }
}