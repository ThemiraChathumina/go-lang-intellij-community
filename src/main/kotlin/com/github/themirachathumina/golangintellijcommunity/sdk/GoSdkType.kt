package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.projectRoots.*
import org.jdom.Element
import javax.swing.Icon

class GoSdkType : SdkType("Go SDK") {
    companion object {
        fun getInstance(): GoSdkType = findInstance(GoSdkType::class.java)
    }

    override fun suggestHomePath(): String? {
        return GoSdkUtils.suggestSdkHomePaths().firstOrNull()
    }

    override fun isValidSdkHome(path: String): Boolean {
        return GoSdkUtils.isValidSdkHome(path)
    }

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String): String {
        val version = getVersionString(sdkHome)
        return if (version != null) "Go $version" else "Go SDK"
    }

    override fun getVersionString(sdkHome: String): String? {
        return GoSdkUtils.retrieveGoVersion(sdkHome)
    }

    override fun createAdditionalDataConfigurable(sdkModel: SdkModel, sdkModificator: SdkModificator): AdditionalDataConfigurable? {
        return null
    }

    override fun getPresentableName(): String = "Go SDK"

    override fun getIcon(): Icon = GoIcons.FILE // Using existing Go icon for now

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {}

    override fun setupSdkPaths(sdk: Sdk, sdkModel: SdkModel): Boolean {
        val modificator = sdk.sdkModificator
        val homePath = sdk.homePath ?: return false
        modificator.versionString = getVersionString(homePath)
        // No need to commit here, the framework handles it
        return true
    }
}
