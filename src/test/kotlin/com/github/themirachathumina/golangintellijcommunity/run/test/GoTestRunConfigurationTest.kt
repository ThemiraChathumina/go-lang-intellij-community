package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoTestRunConfigurationTest : BasePlatformTestCase() {

    fun testCheckConfiguration_ThrowsIfTargetPathIsBlank() {
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test")
        config.targetPath = ""

        try {
            config.checkConfiguration()
            fail("Expected RuntimeConfigurationError")
        } catch (e: RuntimeConfigurationError) {
            assertTrue(e.message!!.contains("Target path is not specified"))
        }
    }

    fun testCheckConfiguration_ThrowsIfSdkNotConfigured() {
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test")
        config.targetPath = "main_test.go"

        try {
            config.checkConfiguration()
            fail("Expected RuntimeConfigurationError")
        } catch (e: RuntimeConfigurationError) {
            assertTrue(e.message!!.contains("Go SDK is not configured for this project"))
        }
    }

    fun testClone() {
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test")
        config.targetPath = "main_test.go"
        config.isVerbose = true
        config.envVariables = mutableMapOf("FOO" to "BAR")

        val cloned = config.clone() as GoTestRunConfiguration
        assertEquals("main_test.go", cloned.targetPath)
        assertTrue(cloned.isVerbose)
        assertEquals("BAR", cloned.envVariables["FOO"])

        // Mutating clone should not mutate original
        cloned.envVariables["FOO"] = "BAZ"
        assertEquals("BAR", config.envVariables["FOO"])
    }
}
