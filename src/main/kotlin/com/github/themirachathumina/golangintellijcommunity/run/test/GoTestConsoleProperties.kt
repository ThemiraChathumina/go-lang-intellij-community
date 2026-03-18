package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTestLocator

/**
 * Console properties for Go tests.
 * Manages the test framework name, locator, and process handler.
 */
class GoTestConsoleProperties(
    config: RunConfiguration,
    executor: Executor
) : SMTRunnerConsoleProperties(config, "GoTest", executor) {

    private var processHandler: ProcessHandler? = null

    init {
        // All our service messages carry 'nodeId' / 'parentNodeId', so use the
        // id-based tree mode for precise parent-child placement.
        isIdBasedTestTree = true
    }

    override fun getTestLocator(): SMTestLocator {
        return GoTestLocator
    }

    /**
     * Set the process handler for the go test process.
     */
    fun setProcessHandler(handler: ProcessHandler) {
        this.processHandler = handler
    }

    /**
     * Provide access to the process handler.
     */
    fun getProcessHandler(): ProcessHandler? {
        return processHandler
    }
}
