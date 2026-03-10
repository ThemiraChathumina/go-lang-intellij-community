package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import java.io.File

@Service(Service.Level.PROJECT)
class GoSdkService(private val project: Project) {
    fun getSdk(): Sdk? {
        val sdk = ProjectRootManager.getInstance(project).projectSdk
        return if (sdk != null && sdk.sdkType is GoSdkType) sdk else null
    }

    fun isSdkValid(): Boolean {
        val sdk = getSdk()
        if (sdk != null && GoSdkUtils.isValidSdkHome(sdk.homePath ?: "")) return true
        
        // Fallback to auto-suggested paths if no SDK is assigned but a valid one exists on system
        return GoSdkUtils.suggestSdkHomePaths().isNotEmpty()
    }

    fun getGoExecutablePath(): String? {
        val sdk = getSdk()
        if (sdk != null) {
            val path = GoSdkUtils.getGoExecutablePath(sdk.homePath ?: "")
            if (path != null) return path
        }
        
        // Fallback to first suggested SDK executable
        val suggested = GoSdkUtils.suggestSdkHomePaths().firstOrNull()
        if (suggested != null) {
            return GoSdkUtils.getGoExecutablePath(suggested)
        }
        
        return null
    }

    fun getGoplsExecutablePath(): String? {
        val sdk = getSdk()
        return GoSdkUtils.getGoplsExecutablePath(sdk?.homePath)
    }
}
