package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoTestSettingsEditorTest : BasePlatformTestCase() {

    fun testSettingsEditorRoundTrip() {
        val editor = GoTestSettingsEditor(project)
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val originalConfig = GoTestRunConfiguration(project, factory, "Test")
        
        originalConfig.testKind = GoTestKind.BENCHMARK
        originalConfig.scope = GoTestScope.PACKAGE
        originalConfig.targetPath = "./pkg"
        originalConfig.isVerbose = true
        originalConfig.useAllCustomBuildTags = true
        originalConfig.moduleName = "github.com/test/mod"
        
        // Push to UI
        editor.resetFrom(originalConfig)
        
        // Pull from UI
        val newConfig = GoTestRunConfiguration(project, factory, "New")
        editor.applyTo(newConfig)
        
        assertEquals(GoTestKind.BENCHMARK, newConfig.testKind)
        assertEquals(GoTestScope.PACKAGE, newConfig.scope)
        assertEquals("./pkg", newConfig.targetPath)
        assertTrue(newConfig.isVerbose)
        assertTrue(newConfig.useAllCustomBuildTags)
        assertEquals("github.com/test/mod", newConfig.moduleName)
    }
}
