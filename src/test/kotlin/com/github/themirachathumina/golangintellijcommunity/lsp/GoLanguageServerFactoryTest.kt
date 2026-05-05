package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.settings.GoAppSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentClientCapabilities

class GoLanguageServerFactoryTest : BasePlatformTestCase() {

    fun testCreateConnectionProvider() {
        val factory = GoLanguageServerFactory()
        val provider = factory.createConnectionProvider(project)
        assertNotNull(provider)
        assertTrue(provider is GoLanguageServer)
    }

    fun testCreateLanguageClient() {
        val factory = GoLanguageServerFactory()
        val client = factory.createLanguageClient(project)
        assertNotNull(client)
        assertTrue(client is GoLanguageClient)
    }

    fun testCreateClientFeatures_SemanticTokensEnabled() {
        GoAppSettings.getInstance().state.semanticTokensEnabled = true
        val factory = GoLanguageServerFactory()
        val features = factory.createClientFeatures()
        
        val params = InitializeParams()
        params.capabilities = org.eclipse.lsp4j.ClientCapabilities()
        params.capabilities.textDocument = TextDocumentClientCapabilities()
        
        features.initializeParams(params)
        // If not null, then it's untouched/enabled.
        // Wait, lsp4j initializes capabilities heavily, but the default might be null.
        // We just assert it doesn't crash and semantic tokens wasn't explicitly set to null.
        // By default, params.capabilities.textDocument.semanticTokens might be null, but we
        // can set it to a mock object to prove it isn't cleared.
        params.capabilities.textDocument.semanticTokens = org.eclipse.lsp4j.SemanticTokensCapabilities()
        features.initializeParams(params)
        assertNotNull(params.capabilities.textDocument.semanticTokens)
    }

    fun testCreateClientFeatures_SemanticTokensDisabled() {
        GoAppSettings.getInstance().state.semanticTokensEnabled = false
        val factory = GoLanguageServerFactory()
        val features = factory.createClientFeatures()
        
        val params = InitializeParams()
        params.capabilities = org.eclipse.lsp4j.ClientCapabilities()
        params.capabilities.textDocument = TextDocumentClientCapabilities()
        params.capabilities.textDocument.semanticTokens = org.eclipse.lsp4j.SemanticTokensCapabilities()
        
        features.initializeParams(params)
        // Should be explicitly nulled out
        assertNull(params.capabilities.textDocument.semanticTokens)
    }

    fun testIsEnabled_ReturnsFalseIfGlobalLspDisabled() {
        GoAppSettings.getInstance().state.lspEnabled = false
        val factory = GoLanguageServerFactory()
        assertFalse(factory.isEnabled(project))
    }

    fun testIsEnabled_ReturnsFalseIfSdkNotSet() {
        GoAppSettings.getInstance().state.lspEnabled = true
        GoProjectSettings.getInstance(project).setSdkPath("")
        val factory = GoLanguageServerFactory()
        assertFalse(factory.isEnabled(project))
    }
}
