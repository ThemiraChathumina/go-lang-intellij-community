package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestProgramRunnerTest : BasePlatformTestCase() {

    fun testGetRunnerId_returnsExpectedId() {
        val runner = GoTestProgramRunner()
        assertEquals("GoTestProgramRunner", runner.getRunnerId())
    }

    fun testCanRun_withGoTestRunConfiguration_andRunExecutor_returnsTrue() {
        val runner = GoTestProgramRunner()
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test")
        assertTrue(runner.canRun(DefaultRunExecutor.EXECUTOR_ID, config))
    }

    fun testCanRun_withGoTestRunConfiguration_andUnknownExecutor_returnsFalse() {
        val runner = GoTestProgramRunner()
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test")
        assertFalse(runner.canRun("SomeUnknownExecutor", config))
    }

    fun testCanRun_withGoTestRunConfiguration_andDebugExecutor_returnsFalse() {
        val runner = GoTestProgramRunner()
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test")
        assertFalse(runner.canRun("Debug", config))
    }

    fun testCanRun_withOtherRunProfile_returnsFalse() {
        val runner = GoTestProgramRunner()
        val otherProfile = object : RunProfile {
            override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState? = null
            override fun getName() = "Other"
            override fun getIcon() = null
        }
        assertFalse(runner.canRun(DefaultRunExecutor.EXECUTOR_ID, otherProfile))
    }

    fun testCanRun_withBenchmarkKind_andRunExecutor_returnsTrue() {
        val runner = GoTestProgramRunner()
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Benchmark")
        config.testKind = GoTestKind.BENCHMARK
        assertTrue(runner.canRun(DefaultRunExecutor.EXECUTOR_ID, config))
    }

    fun testCanRun_withFuzzKind_andRunExecutor_returnsTrue() {
        val runner = GoTestProgramRunner()
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Fuzz")
        config.testKind = GoTestKind.FUZZ
        assertTrue(runner.canRun(DefaultRunExecutor.EXECUTOR_ID, config))
    }
}
