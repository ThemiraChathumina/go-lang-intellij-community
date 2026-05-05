package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoProjectSettingsTest : BasePlatformTestCase() {

    fun testSettingsSerialization() {
        val settings = GoProjectSettings.getInstance(project)
        settings.setSdkPath("/usr/local/go")
        
        assertEquals("/usr/local/go", settings.state.goSdkPath)
        
        // Test resetting
        settings.setSdkPath("")
        assertEquals("", settings.state.goSdkPath)
    }
}
