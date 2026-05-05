package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoRunSettingsEditorTest : BasePlatformTestCase() {

    fun testSettingsEditorRoundTrip() {
        val editor = GoRunSettingsEditor(project)
        val factory = GoRunConfigurationType().configurationFactories[0]
        val originalConfig = GoRunConfiguration(project, factory, "Test")
        
        originalConfig.targetPath = "/path/to/main.go"
        originalConfig.programArguments = "--help"
        originalConfig.goToolArguments = "-v"
        originalConfig.enableRace = true
        originalConfig.buildMode = GoBuildMode.RELEASE
        originalConfig.goOS = "linux"
        originalConfig.cgoEnabled = true
        originalConfig.runInExternalTerminal = true
        
        // Push to UI
        editor.resetFrom(originalConfig)
        
        // Pull from UI
        val newConfig = GoRunConfiguration(project, factory, "New")
        editor.applyTo(newConfig)
        
        assertEquals("/path/to/main.go", newConfig.targetPath)
        assertEquals("--help", newConfig.programArguments)
        assertEquals("-v", newConfig.goToolArguments)
        assertTrue(newConfig.enableRace)
        assertEquals(GoBuildMode.RELEASE, newConfig.buildMode)
        assertEquals("linux", newConfig.goOS)
        assertTrue(newConfig.cgoEnabled)
        assertTrue(newConfig.runInExternalTerminal)
    }
}
