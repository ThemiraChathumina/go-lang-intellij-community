package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider
import org.jetbrains.annotations.NotNull
import java.io.File

class GoLanguageServer(@NotNull project: Project) : OSProcessStreamConnectionProvider() {
    private val LOG = Logger.getInstance(GoLanguageServer::class.java)

    init {
        LOG.info("[Go-Lang-Community] Initializing GoLanguageServer for project: ${project.name}")
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        
        val goplsExe = GoSdkUtils.getGoplsExecutablePath(if (sdkPath.isNotEmpty()) sdkPath else null) ?: "gopls"
        LOG.info("[Go-Lang-Community] Using gopls executable: $goplsExe")
        
        val commandLine = GeneralCommandLine(goplsExe, "serve")
        
        if (sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)) {
            commandLine.withEnvironment("GOROOT", sdkPath)
            val oldPath = System.getenv("PATH") ?: ""
            val binPath = File(sdkPath, "bin").absolutePath
            commandLine.withEnvironment("PATH", "$binPath${File.pathSeparator}$oldPath")
        }

        // Pass GOPATH and GOMODCACHE so gopls resolves modules correctly in non-standard setups.
        // Only set if they differ from what the process would inherit from the system environment,
        // so we don't override a user's already-correct environment.
        val effectiveGoPath = GoSdkUtils.resolveGoPath().joinToString(File.pathSeparator)
        val systemGoPath = System.getenv("GOPATH") ?: ""
        if (effectiveGoPath != systemGoPath) {
            commandLine.withEnvironment("GOPATH", effectiveGoPath)
            LOG.info("[Go-Lang-Community] Setting GOPATH for gopls: $effectiveGoPath")
        }

        val effectiveGoModCache = GoSdkUtils.resolveGoModCache()
        val systemGoModCache = System.getenv("GOMODCACHE") ?: ""
        if (effectiveGoModCache != systemGoModCache) {
            commandLine.withEnvironment("GOMODCACHE", effectiveGoModCache)
            LOG.info("[Go-Lang-Community] Setting GOMODCACHE for gopls: $effectiveGoModCache")
        }

        super.setCommandLine(commandLine)
    }
}
