package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoBenchmarkRunConfigurationProducerTest : BasePlatformTestCase() {

    private val producer = GoBenchmarkRunConfigurationProducer()

    private fun setup(code: String, filename: String = "main_test.go"): GoTestRunConfiguration? {
        myFixture.configureByText(filename, code)
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return null
        return producer.findOrCreateConfigurationFromContext(ConfigurationContext(element))
            ?.configuration as? GoTestRunConfiguration
    }

    // --- setupConfigurationFromContext ---

    fun testSetup_nonTestFile_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc BenchmarkFoo() {}", filename = "main.go"))
    }

    fun testSetup_insideBenchmarkFoo_setsFunctionScope() {
        val config = setup("package main\n\nfunc <caret>BenchmarkFoo() {}")
        assertNotNull(config)
        assertEquals(GoTestScope.FUNCTION, config!!.scope)
        assertEquals(GoTestKind.BENCHMARK, config.testKind)
        assertEquals(".:BenchmarkFoo", config.targetPath)
    }

    fun testSetup_insideTestFoo_returnsNull() {
        assertNull(setup("package main\n\nfunc <caret>TestFoo() {}"))
    }

    fun testSetup_fileLevelOnlyBenchmarks_setsFileScope() {
        val config = setup("package <caret>main\n\nfunc BenchmarkFoo() {}")
        assertNotNull(config)
        assertEquals(GoTestScope.FILE, config!!.scope)
        assertEquals(GoTestKind.BENCHMARK, config.testKind)
    }

    fun testSetup_fileLevelWithTests_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc TestFoo() {}\nfunc BenchmarkFoo() {}"))
    }

    fun testSetup_fileLevelNoBenchmarks_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc TestFoo() {}"))
    }

    // --- isConfigurationFromContext ---

    fun testIsConfig_matchingBenchmark_returnsTrue() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>BenchmarkFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.BENCHMARK
            scope = GoTestScope.FUNCTION
            targetPath = ".:BenchmarkFoo"
        }
        assertTrue(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }

    fun testIsConfig_wrongKind_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>BenchmarkFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.TEST
            scope = GoTestScope.FUNCTION
            targetPath = ".:BenchmarkFoo"
        }
        assertFalse(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }

    fun testIsConfig_differentTargetPath_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>BenchmarkFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.BENCHMARK
            scope = GoTestScope.FUNCTION
            targetPath = ".:BenchmarkBar"
        }
        assertFalse(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }
}
