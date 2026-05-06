package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfigurationType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoCoverageEnabledConfigurationTest : BasePlatformTestCase() {

    @Suppress("DEPRECATION")
    fun testCoverageConfiguration() {
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfiguration(project, factory, "Test")
        
        val coverageConfig = GoCoverageEnabledConfiguration(config)
        
        // Assert initial state is set from GoCoverageEnabledConfiguration defaults
        assertFalse(coverageConfig.isCoverageEnabled) 
        
        coverageConfig.isCoverageEnabled = true
        assertTrue(coverageConfig.isCoverageEnabled)
    }
}
