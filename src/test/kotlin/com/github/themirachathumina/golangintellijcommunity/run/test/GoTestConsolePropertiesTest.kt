package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.OutputStream

class GoTestConsolePropertiesTest : BasePlatformTestCase() {

    private fun makeProperties(): GoTestConsoleProperties {
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test")
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        return GoTestConsoleProperties(config, executor)
    }

    private fun dummyHandler(): ProcessHandler = object : ProcessHandler() {
        override fun destroyProcessImpl() {}
        override fun detachProcessImpl() {}
        override fun detachIsDefault(): Boolean = false
        override fun getProcessInput(): OutputStream? = null
    }

    fun testGetTestLocator_returnsGoTestLocator() {
        val props = makeProperties()
        assertSame(GoTestLocator, props.getTestLocator())
    }

    fun testGetProcessHandler_initiallyNull() {
        val props = makeProperties()
        assertNull(props.getProcessHandler())
    }

    fun testSetProcessHandler_canBeRetrieved() {
        val props = makeProperties()
        val handler = dummyHandler()
        props.setProcessHandler(handler)
        assertSame(handler, props.getProcessHandler())
    }

    fun testSetProcessHandler_canBeOverwritten() {
        val props = makeProperties()
        val handler1 = dummyHandler()
        val handler2 = dummyHandler()

        props.setProcessHandler(handler1)
        props.setProcessHandler(handler2)

        assertSame(handler2, props.getProcessHandler())
        assertNotSame(handler1, props.getProcessHandler())
    }

    fun testConstructor_doesNotThrow() {
        assertNotNull(makeProperties())
    }
}
