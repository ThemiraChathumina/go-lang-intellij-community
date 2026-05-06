package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.run.GoRunConfigurationType
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfigurationType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoCoverageEngineTest : BasePlatformTestCase() {

    fun testIsApplicableTo() {
        val engine = GoCoverageEngine()
        
        val testFactory = GoTestRunConfigurationType().configurationFactories[0]
        val testConfig = com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfiguration(project, testFactory, "Test")
        assertTrue(engine.isApplicableTo(testConfig))
        
        val runFactory = GoRunConfigurationType().configurationFactories[0]
        val runConfig = com.github.themirachathumina.golangintellijcommunity.run.GoRunConfiguration(project, runFactory, "Run")
        assertTrue(engine.isApplicableTo(runConfig))
    }

    fun testIsProductionGoFile() {
        val prodFile = myFixture.configureByText("main.go", "package main").virtualFile
        val testFile = myFixture.configureByText("main_test.go", "package main").virtualFile
        val notGoFile = myFixture.configureByText("main.txt", "package main").virtualFile
        
        assertTrue(GoCoverageEngine.isProductionGoFile(prodFile))
        assertFalse(GoCoverageEngine.isProductionGoFile(testFile))
        assertFalse(GoCoverageEngine.isProductionGoFile(notGoFile))
    }
}
