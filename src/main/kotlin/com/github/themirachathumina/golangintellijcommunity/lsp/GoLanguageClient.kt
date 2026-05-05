package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.settings.GoAppSettings
import com.google.gson.JsonObject
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.client.LanguageClientImpl

class GoLanguageClient(project: Project) : LanguageClientImpl(project) {

    /**
     * Called by lsp4ij when gopls sends workspace/configuration { section: "gopls" }.
     * Returns a root JsonObject keyed by section so findSettings("gopls", ...) resolves correctly.
     */
    override fun createSettings(): Any {
        val goplsSettings = JsonObject().apply {
            addProperty("semanticTokens", GoAppSettings.getInstance().state.semanticTokensEnabled)
        }
        return JsonObject().apply { add(GoConstants.Lsp.SERVER_ID, goplsSettings) }
    }
}
