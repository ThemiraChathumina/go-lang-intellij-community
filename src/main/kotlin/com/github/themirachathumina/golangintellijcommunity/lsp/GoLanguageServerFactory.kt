package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkService
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.LanguageServerEnablementSupport
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl
import com.redhat.devtools.lsp4ij.client.features.LSPClientFeatures
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import org.eclipse.lsp4j.InitializeParams
import org.jetbrains.annotations.NotNull

class GoLanguageServerFactory : LanguageServerFactory, LanguageServerEnablementSupport {
    override fun createConnectionProvider(@NotNull project: Project): StreamConnectionProvider {
        return GoLanguageServer(project)
    }

    override fun createLanguageClient(@NotNull project: Project): LanguageClientImpl {
        return LanguageClientImpl(project)
    }

    override fun createClientFeatures(): @NotNull LSPClientFeatures {
        return object : LSPClientFeatures() {
            override fun initializeParams(@NotNull params: InitializeParams) {
                super.initializeParams(params)
                params.capabilities?.textDocument?.semanticTokens = null
            }
        }
    }

    override fun isEnabled(@NotNull project: Project): Boolean {
        return project.getService(GoSdkService::class.java).isSdkValid()
    }

    override fun setEnabled(enabled: Boolean, @NotNull project: Project) {
        // Handled by project settings
    }
}
