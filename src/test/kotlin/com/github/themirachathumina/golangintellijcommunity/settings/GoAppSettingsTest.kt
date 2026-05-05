package com.github.themirachathumina.golangintellijcommunity.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoAppSettingsTest : BasePlatformTestCase() {

    fun testAppSettings() {
        val settings = GoAppSettings.getInstance()
        val initialState = settings.state.lspEnabled
        
        settings.state.lspEnabled = !initialState
        assertEquals(!initialState, settings.state.lspEnabled)
        
        // Restore
        settings.state.lspEnabled = initialState
    }
}
