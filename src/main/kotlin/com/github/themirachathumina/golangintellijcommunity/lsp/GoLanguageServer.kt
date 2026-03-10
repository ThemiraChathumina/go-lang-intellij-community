package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider
import org.jetbrains.annotations.NotNull

class GoLanguageServer(@NotNull project: Project) : OSProcessStreamConnectionProvider() {
    init {
        val sdkService = project.getService(GoSdkService::class.java)
        val goplsExe = sdkService.getGoplsExecutablePath() ?: "gopls"
        
        val commandLine = GeneralCommandLine(goplsExe, "serve")
        super.setCommandLine(commandLine)
    }
}
