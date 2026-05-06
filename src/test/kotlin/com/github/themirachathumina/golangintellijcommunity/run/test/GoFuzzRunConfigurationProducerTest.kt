package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoFuzzRunConfigurationProducerTest : BasePlatformTestCase() {

    private val producer = GoFuzzRunConfigurationProducer()

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
        assertNull(setup("package <caret>main\n\nfunc FuzzFoo() {}", filename = "main.go"))
    }

    fun testSetup_insideFuzzFoo_withoutBypass_returnsNull() {
        assertNull(setup("package main\n\nfunc <caret>FuzzFoo() {}"))
    }

    fun testSetup_insideFuzzFoo_withBypass_setsFunctionScope() {
        val config = setup("package main\n\nfunc <caret>FuzzFoo() {}", bypass = true)
        assertNotNull(config)
        assertEquals(GoTestScope.FUNCTION, config!!.scope)
        assertEquals(GoTestKind.FUZZ, config.testKind)
        assertEquals(".:FuzzFoo", config.targetPath)
    }

    fun testSetup_insideTestFoo_withBypass_returnsNull() {
        assertNull(setup("package main\n\nfunc <caret>TestFoo() {}", bypass = true))
    }

    fun testSetup_fileLevelFuzzOnly_withoutBypass_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc FuzzFoo() {}"))
    }

    fun testSetup_fileLevelFuzzOnly_withBypass_setsFileScope() {
        val config = setup("package <caret>main\n\nfunc FuzzFoo() {}", bypass = true)
        assertNotNull(config)
        assertEquals(GoTestScope.FILE, config!!.scope)
        assertEquals(GoTestKind.FUZZ, config.testKind)
    }

    fun testSetup_fileLevelNoFuzz_withBypass_returnsNull() {
        assertNull(setup("package <caret>main\n\nfunc TestFoo() {}", bypass = true))
    }

    // --- isConfigurationFromContext ---

    fun testIsConfig_matchingFuzz_returnsTrue() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>FuzzFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.FUZZ
            scope = GoTestScope.FUNCTION
            targetPath = ".:FuzzFoo"
        }
        assertTrue(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }

    fun testIsConfig_wrongKind_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>FuzzFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.TEST
            scope = GoTestScope.FUNCTION
            targetPath = ".:FuzzFoo"
        }
        assertFalse(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }

    fun testIsConfig_differentTargetPath_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>FuzzFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val config = GoTestRunConfiguration(project, GoTestRunConfigurationType().configurationFactories[0], "t").apply {
            testKind = GoTestKind.FUZZ
            scope = GoTestScope.FUNCTION
            targetPath = ".:FuzzBar"
        }
        assertFalse(producer.isConfigurationFromContext(config, ConfigurationContext(element)))
    }
}
