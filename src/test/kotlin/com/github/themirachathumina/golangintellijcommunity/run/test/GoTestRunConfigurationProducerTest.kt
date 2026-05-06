package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestRunConfigurationProducerTest : BasePlatformTestCase() {

    private val producer = GoTestRunConfigurationProducer()

    private fun setup(code: String, filename: String = "main_test.go", bypass: Boolean = false): GoTestRunConfiguration? {
        myFixture.configureByText(filename, code)
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return null
        val context = ConfigurationContext(element)
        val result = if (bypass) GoTestProducerGate.withBypass { producer.findOrCreateConfigurationFromContext(context) }
                     else producer.findOrCreateConfigurationFromContext(context)
        return result?.configuration as? GoTestRunConfiguration
    }

    // --- setupConfigurationFromContext ---

    fun testSetup_nonTestFile_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc TestFoo() {}", filename = "main.go"))
    }

    fun testSetup_insideTestFooName_setsFunctionScope() {
        val config = setup("package main\n\nfunc <caret>TestFoo() {}")
        assertNotNull(config)
        assertEquals(GoTestScope.FUNCTION, config!!.scope)
        assertEquals(GoTestKind.TEST, config.testKind)
        assertEquals(".:TestFoo", config.targetPath)
    }

    fun testSetup_insideTestMainName_setsPackageScope() {
        val config = setup("package main\n\nfunc <caret>TestMain() {}")
        assertNotNull(config)
        assertEquals(GoTestScope.PACKAGE, config!!.scope)
        assertEquals(GoTestKind.TEST, config.testKind)
        assertEquals(".", config.targetPath)
    }

    fun testSetup_insideBenchmarkFoo_returnsNull() {
        assertNull(setup("package main\n\nfunc <caret>BenchmarkFoo() {}"))
    }

    fun testSetup_insideFuzzFoo_returnsNull() {
        assertNull(setup("package main\n\nfunc <caret>FuzzFoo() {}"))
    }

    fun testSetup_fileLevelOnlyTests_setsFileScope() {
        val config = setup("package <caret>main\n\nfunc TestFoo() {}")
        assertNotNull(config)
        assertEquals(GoTestScope.FILE, config!!.scope)
        assertEquals(GoTestKind.TEST, config.testKind)
    }

    fun testSetup_fileLevelWithBenchmarks_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc TestFoo() {}\nfunc BenchmarkFoo() {}"))
    }

    fun testSetup_fileLevelNoTestFunctions_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc helperFunc() {}"))
    }

    // --- isConfigurationFromContext ---

    fun testIsConfig_matchingFunctionScope_returnsTrue() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>TestFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.TEST
            scope = GoTestScope.FUNCTION
            targetPath = ".:TestFoo"
        }
        assertTrue(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }

    fun testIsConfig_wrongKind_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>TestFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.BENCHMARK
            scope = GoTestScope.FUNCTION
            targetPath = ".:TestFoo"
        }
        assertFalse(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }

    fun testIsConfig_differentTargetPath_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>TestFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.TEST
            scope = GoTestScope.FUNCTION
            targetPath = ".:TestBar"
        }
        assertFalse(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }

    fun testIsConfig_fileScope_returnsTrue() {
        myFixture.configureByText("main_test.go", "package <caret>main\n\nfunc TestFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val virtualFilePath = myFixture.file.virtualFile.path
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.TEST
            scope = GoTestScope.FILE
            targetPath = virtualFilePath
        }
        assertTrue(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }
}
