package com.github.themirachathumina.golangintellijcommunity.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider
import org.jetbrains.annotations.NotNull

class GoLanguageServer(@NotNull project: Project) : OSProcessStreamConnectionProvider() {
    init {
        // Assume gopls is in the PATH
        val commandLine = GeneralCommandLine("gopls", "serve")
        super.setCommandLine(commandLine)
    }
}
