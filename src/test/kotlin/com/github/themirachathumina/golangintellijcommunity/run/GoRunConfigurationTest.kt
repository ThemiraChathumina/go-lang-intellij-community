package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoRunConfigurationTest : BasePlatformTestCase() {

    fun testCheckConfiguration_ThrowsIfTargetPathIsBlank() {
        val factory = GoRunConfigurationType().configurationFactories[0]
        val config = GoRunConfiguration(project, factory, "Test")
        config.targetPath = ""

        try {
            config.checkConfiguration()
            fail("Expected RuntimeConfigurationError")
        } catch (e: RuntimeConfigurationError) {
            assertTrue(e.message!!.contains("Target path is not specified"))
        }
    }

    fun testCheckConfiguration_ThrowsIfSdkNotConfigured() {
        val factory = GoRunConfigurationType().configurationFactories[0]
        val config = GoRunConfiguration(project, factory, "Test")
        config.targetPath = "main.go"

        try {
            config.checkConfiguration()
            fail("Expected RuntimeConfigurationError")
        } catch (e: RuntimeConfigurationError) {
            assertTrue(e.message!!.contains("Go SDK is not configured for this project"))
        }
    }

    fun testClone() {
        val factory = GoRunConfigurationType().configurationFactories[0]
        val config = GoRunConfiguration(project, factory, "Test")
        config.targetPath = "main.go"
        config.goOS = "linux"
        config.goArch = "amd64"
        config.envVariables = mutableMapOf("FOO" to "BAR")

        val cloned = config.clone() as GoRunConfiguration
        assertEquals("main.go", cloned.targetPath)
        assertEquals("linux", cloned.goOS)
        assertEquals("amd64", cloned.goArch)
        assertEquals("BAR", cloned.envVariables["FOO"])

        // Mutating clone should not mutate original
        cloned.envVariables["FOO"] = "BAZ"
        assertEquals("BAR", config.envVariables["FOO"])
    }
}
