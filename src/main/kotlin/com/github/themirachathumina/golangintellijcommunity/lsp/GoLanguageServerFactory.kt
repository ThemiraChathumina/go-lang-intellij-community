package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.LanguageServerEnablementSupport
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl
import com.github.themirachathumina.golangintellijcommunity.settings.GoAppSettings
import com.intellij.psi.PsiFile
import com.redhat.devtools.lsp4ij.client.features.LSPClientFeatures
import com.redhat.devtools.lsp4ij.client.features.LSPSelectionRangeFeature
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import org.eclipse.lsp4j.InitializeParams
import org.jetbrains.annotations.NotNull

class GoLanguageServerFactory : LanguageServerFactory, LanguageServerEnablementSupport {
    private val LOG = Logger.getInstance(GoLanguageServerFactory::class.java)

    override fun createConnectionProvider(@NotNull project: Project): StreamConnectionProvider {
        LOG.info("[Go-Lang-Community] Creating connection provider for ${project.name}")
        return GoLanguageServer(project)
    }

    override fun createLanguageClient(@NotNull project: Project): LanguageClientImpl {
        LOG.debug("[Go-Lang-Community] Creating language client for ${project.name}")
        return GoLanguageClient(project)
    }

    override fun createClientFeatures(): @NotNull LSPClientFeatures {
        return object : LSPClientFeatures() {
            override fun initializeParams(@NotNull params: InitializeParams) {
                super.initializeParams(params)
                if (!GoAppSettings.getInstance().state.semanticTokensEnabled) {
                    LOG.debug("[Go-Lang-Community] Semantic tokens disabled by settings — nulling capability")
                    params.capabilities?.textDocument?.semanticTokens = null
                } else {
                    LOG.debug("[Go-Lang-Community] Semantic tokens enabled by settings")
                }
            }

            init {
                setSelectionRangeFeature(object : LSPSelectionRangeFeature() {
                    override fun isSelectionRangeSupported(file: PsiFile): Boolean {
                        val name = file.virtualFile?.name ?: return super.isSelectionRangeSupported(file)
                        if (name == "go.mod" || name == "go.work") return false
                        return super.isSelectionRangeSupported(file)
                    }
                })
            }
        }
    }

    override fun isEnabled(@NotNull project: Project): Boolean {
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        val isValid = sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)
        LOG.debug("[Go-Lang-Community] Checking if Go language server is enabled for ${project.name}: $isValid")
        return isValid
    }

    override fun setEnabled(enabled: Boolean, @NotNull project: Project) {
        LOG.info("[Go-Lang-Community] Setting Go language server enabled: $enabled for ${project.name}")
        // Handled by project settings
    }
}
