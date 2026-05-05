package com.github.themirachathumina.golangintellijcommunity.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoLanguageConfigurableTest : BasePlatformTestCase() {

    fun testConfigurable() {
        val configurable = GoLanguageConfigurable()
        
        // This instantiates the UI panel
        val panel = configurable.createComponent()
        assertNotNull(panel)

        // By default, it shouldn't be modified until user interacts
        assertFalse(configurable.isModified)

        // Reset pulls from current state
        configurable.reset()
        assertFalse(configurable.isModified)
        
        // Settings are applied correctly
        // Note: apply() actually restarts language server which we might not want to do fully in unit test
        // without mocking, but calling createComponent is safe and tests the UI DSL builds without exceptions.
    }
}
