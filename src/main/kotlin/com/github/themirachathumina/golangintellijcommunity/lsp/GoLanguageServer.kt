package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider
import org.jetbrains.annotations.NotNull

class GoLanguageServer(@NotNull project: Project) : OSProcessStreamConnectionProvider() {
    private val LOG = Logger.getInstance(GoLanguageServer::class.java)

    init {
        LOG.info("[Go-Lang-Community] Initializing GoLanguageServer for project: ${project.name}")
        val sdkService = project.getService(GoSdkService::class.java)
        val goplsExe = sdkService.getGoplsExecutablePath() ?: "gopls"
        
        LOG.info("[Go-Lang-Community] Using gopls executable: $goplsExe")
        val commandLine = GeneralCommandLine(goplsExe, "serve")
        super.setCommandLine(commandLine)
    }
}
