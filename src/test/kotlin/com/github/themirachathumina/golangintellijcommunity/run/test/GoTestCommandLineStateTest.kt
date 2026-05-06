package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestCommandLineStateTest : BasePlatformTestCase() {

    private val exe = "/usr/local/go/bin/go"

    private fun makeState(configurer: GoTestRunConfiguration.() -> Unit = {}): GoTestCommandLineState {
        val factory = GoTestRunConfigurationType().configurationFactories[0]
        val config = GoTestRunConfiguration(project, factory, "Test").apply(configurer)
        val runnerSettings = RunManager.getInstance(project).createConfiguration("test", factory)
        val env = ExecutionEnvironmentBuilder
            .create(DefaultRunExecutor.getRunExecutorInstance(), runnerSettings)
            .build()
        return GoTestCommandLineState(env, config)
    }

    private fun params(state: GoTestCommandLineState) =
        state.buildGoTestCommandLine(exe).parametersList.list

    // --- always-present flags ---

    fun testBuildCommandLine_alwaysHasTestAndJsonFlags() {
        val params = params(makeState())
        assertTrue(params.contains("test"))
        assertTrue(params.contains("-json"))
    }

    // --- optional flags ---

    fun testBuildCommandLine_verbose_addsVFlag() {
        val params = params(makeState { isVerbose = true })
        assertTrue(params.contains("-v"))
    }

    fun testBuildCommandLine_notVerbose_noVFlag() {
        val params = params(makeState { isVerbose = false })
        assertFalse(params.contains("-v"))
    }

    fun testBuildCommandLine_short_addsShortFlag() {
        val params = params(makeState { isShort = true })
        assertTrue(params.contains("-short"))
    }

    fun testBuildCommandLine_race_addsRaceFlag() {
        val params = params(makeState { isRace = true })
        assertTrue(params.contains("-race"))
    }

    // --- build tags ---

    fun testBuildCommandLine_withBuildTags_addsTagsFlag() {
        val params = params(makeState { buildTags = "integration" })
        assertTrue(params.contains("-tags"))
        assertTrue(params.contains("integration"))
    }

    fun testBuildCommandLine_emptyBuildTags_noTagsFlag() {
        val params = params(makeState { buildTags = "" })
        assertFalse(params.contains("-tags"))
    }

    // --- coverage ---

    fun testBuildCommandLine_withCoverageFilePath_addsCoverFlags() {
        val state = makeState()
        val cmdLine = state.buildGoTestCommandLine(exe)
        // coverageFilePath defaults to null so no -coverprofile
        assertFalse(cmdLine.parametersList.list.any { it.startsWith("-coverprofile") })
    }

    // --- go tool arguments ---

    fun testBuildCommandLine_withGoToolArgs_splitsAndAdds() {
        val params = params(makeState { goToolArguments = "-count=1 -timeout=30s" })
        assertTrue(params.contains("-count=1"))
        assertTrue(params.contains("-timeout=30s"))
    }

    fun testBuildCommandLine_blankGoToolArgs_nothingAdded() {
        val params = params(makeState { goToolArguments = "" })
        assertFalse(params.any { it.startsWith("-count") })
    }

    // --- DIRECTORY scope ---

    fun testBuildCommandLine_directoryScope_test_addsDotDotDot() {
        val params = params(makeState {
            scope = GoTestScope.DIRECTORY
            testKind = GoTestKind.TEST
            targetPath = "/some/dir"
        })
        assertTrue(params.contains("./..."))
        assertFalse(params.contains("-run=^$"))
    }

    fun testBuildCommandLine_directoryScope_benchmark_addsRunAndBench() {
        val params = params(makeState {
            scope = GoTestScope.DIRECTORY
            testKind = GoTestKind.BENCHMARK
            targetPath = "/some/dir"
        })
        assertTrue(params.contains("./..."))
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-bench=."))
    }

    fun testBuildCommandLine_directoryScope_fuzz_addsRunAndFuzz() {
        val params = params(makeState {
            scope = GoTestScope.DIRECTORY
            testKind = GoTestKind.FUZZ
            targetPath = "/some/dir"
        })
        assertTrue(params.contains("./..."))
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-fuzz=."))
    }

    // --- PACKAGE scope ---

    fun testBuildCommandLine_packageScope_test_addsTargetPath() {
        val params = params(makeState {
            scope = GoTestScope.PACKAGE
            testKind = GoTestKind.TEST
            targetPath = "./mypkg"
        })
        assertTrue(params.contains("./mypkg"))
        assertFalse(params.contains("-run=^$"))
    }

    fun testBuildCommandLine_packageScope_benchmark_addsRunAndBench() {
        val params = params(makeState {
            scope = GoTestScope.PACKAGE
            testKind = GoTestKind.BENCHMARK
            targetPath = "./mypkg"
        })
        assertTrue(params.contains("./mypkg"))
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-bench=."))
    }

    fun testBuildCommandLine_packageScope_fuzz_addsRunAndFuzz() {
        val params = params(makeState {
            scope = GoTestScope.PACKAGE
            testKind = GoTestKind.FUZZ
            targetPath = "./mypkg"
        })
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-fuzz=."))
    }

    // --- FILE scope ---

    fun testBuildCommandLine_fileScope_test_addsDot() {
        val params = params(makeState {
            scope = GoTestScope.FILE
            testKind = GoTestKind.TEST
        })
        assertTrue(params.contains("."))
        assertFalse(params.contains("-bench=."))
    }

    fun testBuildCommandLine_fileScope_benchmark_addsRunAndBench() {
        val params = params(makeState {
            scope = GoTestScope.FILE
            testKind = GoTestKind.BENCHMARK
        })
        assertTrue(params.contains("."))
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-bench=."))
    }

    fun testBuildCommandLine_fileScope_fuzz_addsRunAndFuzz() {
        val params = params(makeState {
            scope = GoTestScope.FILE
            testKind = GoTestKind.FUZZ
        })
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-fuzz=."))
    }

    // --- FUNCTION scope ---

    fun testBuildCommandLine_functionScope_test_withColon_addsRunFlag() {
        val params = params(makeState {
            scope = GoTestScope.FUNCTION
            testKind = GoTestKind.TEST
            targetPath = ".:TestFoo"
        })
        assertTrue(params.contains("."))
        assertTrue(params.contains("-run=^TestFoo$"))
    }

    fun testBuildCommandLine_functionScope_benchmark_withColon_addsBenchFlag() {
        val params = params(makeState {
            scope = GoTestScope.FUNCTION
            testKind = GoTestKind.BENCHMARK
            targetPath = ".:BenchmarkFoo"
        })
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-bench=^BenchmarkFoo$"))
    }

    fun testBuildCommandLine_functionScope_fuzz_withColon_addsFuzzFlag() {
        val params = params(makeState {
            scope = GoTestScope.FUNCTION
            testKind = GoTestKind.FUZZ
            targetPath = ".:FuzzFoo"
        })
        assertTrue(params.contains("-run=^$"))
        assertTrue(params.contains("-fuzz=^FuzzFoo$"))
    }

    fun testBuildCommandLine_functionScope_noColon_usesTargetPathDirectly() {
        val params = params(makeState {
            scope = GoTestScope.FUNCTION
            testKind = GoTestKind.TEST
            targetPath = "./mypkg"
        })
        assertTrue(params.contains("./mypkg"))
    }

    fun testBuildCommandLine_functionScope_benchmark_noColon_addsBenchDot() {
        val params = params(makeState {
            scope = GoTestScope.FUNCTION
            testKind = GoTestKind.BENCHMARK
            targetPath = "./mypkg"
        })
        assertTrue(params.contains("-bench=."))
    }

    fun testBuildCommandLine_functionScope_fuzz_noColon_addsFuzzDot() {
        val params = params(makeState {
            scope = GoTestScope.FUNCTION
            testKind = GoTestKind.FUZZ
            targetPath = "./mypkg"
        })
        assertTrue(params.contains("-fuzz=."))
    }

    // --- program arguments ---

    fun testBuildCommandLine_withProgramArgs_addsDashDashAndArgs() {
        val params = params(makeState { programArguments = "-myarg value" })
        val dashIdx = params.indexOf("--")
        assertTrue("Expected '--' separator", dashIdx >= 0)
        assertTrue(params.contains("-myarg"))
        assertTrue(params.contains("value"))
    }

    fun testBuildCommandLine_blankProgramArgs_noDashDash() {
        val params = params(makeState { programArguments = "" })
        assertFalse(params.contains("--"))
    }

    // --- executable is first parameter ---

    fun testBuildCommandLine_executableIsFirstEntry() {
        val cmdLine = makeState().buildGoTestCommandLine(exe)
        assertEquals(exe, cmdLine.exePath)
    }

    // --- env variables ---

    fun testBuildCommandLine_envVariables_arePropagated() {
        val state = makeState { envVariables = mutableMapOf("MY_VAR" to "val") }
        val cmdLine = state.buildGoTestCommandLine(exe)
        assertEquals("val", cmdLine.environment["MY_VAR"])
    }
}
