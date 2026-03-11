package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import java.io.File

@Service(Service.Level.PROJECT)
class GoSdkService(private val project: Project) {
    private val LOG = Logger.getInstance(GoSdkService::class.java)

    fun getSdk(): Sdk? {
        val sdk = ProjectRootManager.getInstance(project).projectSdk
        val isGo = sdk != null && sdk.sdkType is GoSdkType
        LOG.debug("[Go-Lang-Community] Getting SDK for ${project.name}: ${sdk?.name} (isGo=$isGo)")
        return if (isGo) sdk else null
    }

    fun isSdkValid(): Boolean {
        val sdk = getSdk()
        if (sdk != null && GoSdkUtils.isValidSdkHome(sdk.homePath ?: "")) {
            LOG.debug("[Go-Lang-Community] SDK is valid for ${project.name}")
            return true
        }
        
        // Fallback to auto-suggested paths if no SDK is assigned but a valid one exists on system
        val hasSuggested = GoSdkUtils.suggestSdkHomePaths().isNotEmpty()
        LOG.debug("[Go-Lang-Community] No valid SDK assigned, has suggested: $hasSuggested")
        return hasSuggested
    }

    fun getGoExecutablePath(): String? {
        val sdk = getSdk()
        if (sdk != null) {
            val path = GoSdkUtils.getGoExecutablePath(sdk.homePath ?: "")
            if (path != null) {
                LOG.debug("[Go-Lang-Community] Go executable from SDK: $path")
                return path
            }
        }
        
        // Fallback to first suggested SDK executable
        val suggested = GoSdkUtils.suggestSdkHomePaths().firstOrNull()
        if (suggested != null) {
            val path = GoSdkUtils.getGoExecutablePath(suggested)
            LOG.debug("[Go-Lang-Community] Go executable from suggested path: $path")
            return path
        }
        
        LOG.warn("[Go-Lang-Community] No Go executable found for ${project.name}")
        return null
    }

    fun getGoplsExecutablePath(): String? {
        val sdk = getSdk()
        val path = GoSdkUtils.getGoplsExecutablePath(sdk?.homePath)
        LOG.debug("[Go-Lang-Community] Gopls executable path: $path")
        return path
    }
}
