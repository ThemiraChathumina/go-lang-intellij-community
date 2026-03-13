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
        
        super.setCommandLine(commandLine)
    }
}
