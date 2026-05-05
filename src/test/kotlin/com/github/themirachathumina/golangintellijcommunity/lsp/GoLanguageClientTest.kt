package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.settings.GoAppSettings
import com.google.gson.JsonObject
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoLanguageClientTest : BasePlatformTestCase() {

    fun testCreateSettings_SemanticTokensEnabled() {
        GoAppSettings.getInstance().state.semanticTokensEnabled = true
        val client = GoLanguageClient(project)
        val settings = client.createSettings() as JsonObject
        
        assertTrue(settings.has("gopls"))
        val goplsSettings = settings.getAsJsonObject("gopls")
        assertTrue(goplsSettings.get("semanticTokens").asBoolean)
    }

    fun testCreateSettings_SemanticTokensDisabled() {
        GoAppSettings.getInstance().state.semanticTokensEnabled = false
        val client = GoLanguageClient(project)
        val settings = client.createSettings() as JsonObject
        
        assertTrue(settings.has("gopls"))
        val goplsSettings = settings.getAsJsonObject("gopls")
        assertFalse(goplsSettings.get("semanticTokens").asBoolean)
    }
}
