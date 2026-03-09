package com.github.themirachathumina.golangintellijcommunity.lsp

import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl
import com.redhat.devtools.lsp4ij.client.features.LSPClientFeatures
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import org.eclipse.lsp4j.InitializeParams
import org.jetbrains.annotations.NotNull

class GoLanguageServerFactory : LanguageServerFactory {
    override fun createConnectionProvider(@NotNull project: Project): StreamConnectionProvider {
        return GoLanguageServer(project)
    }

    override fun createLanguageClient(@NotNull project: Project): LanguageClientImpl {
        // Return the default implementation as customization is now done via LSPClientFeatures
        return LanguageClientImpl(project)
    }

    override fun createClientFeatures(): @NotNull LSPClientFeatures {
        return object : LSPClientFeatures() {
            override fun initializeParams(@NotNull params: InitializeParams) {
                super.initializeParams(params)
                // Avoid semantic token request by modifying client capabilities
                params.capabilities?.textDocument?.semanticTokens = null
            }
        }
    }
}
