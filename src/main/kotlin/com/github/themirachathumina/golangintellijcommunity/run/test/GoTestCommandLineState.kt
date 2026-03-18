package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.execution.ParametersListUtil

class GoTestCommandLineState(
    env: ExecutionEnvironment,
    private val config: GoTestRunConfiguration
) : CommandLineState(env) {

    /** Coverage profile path; set only when the Coverage executor is used. */
    private var coverageFilePath: String? = null

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        // When running via the Coverage executor, retrieve (or create) the
        // CoverageEnabledConfiguration for this run config so we can get the
        // temp .out file path to pass to `go test -coverprofile=...`.
        if (executor is CoverageExecutor) {
            val covConfig = CoverageEnabledConfiguration.getOrCreate(config)
            coverageFilePath = covConfig.getCoverageFilePath()
        }

        val processHandler = startProcess()
        val consoleProperties = GoTestConsoleProperties(config, executor)
        consoleProperties.setProcessHandler(processHandler)

        val consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(
            "GoTest", processHandler, consoleProperties
        )

        consoleView.addMessageFilter(GoConsoleFilter(config.project, config.workingDirectory))

        // Refresh gutter icons in all open editors once the test run finishes.
        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                ApplicationManager.getApplication().invokeLater {
                    if (!config.project.isDisposed) {
                        DaemonCodeAnalyzer.getInstance(config.project).restart()
                    }
                }
            }
        })

        return com.intellij.execution.DefaultExecutionResult(consoleView, processHandler)
    }

    override fun startProcess(): ProcessHandler {
        val sdkPath = GoProjectSettings.getInstance(config.project).state.goSdkPath
        val goExe = GoSdkUtils.getGoExecutablePath(sdkPath)
            ?: throw com.intellij.execution.ExecutionException(
                "Go executable not found in SDK path: $sdkPath"
            )

        val commandLine = GeneralCommandLine(goExe)
        commandLine.addParameter("test")
        commandLine.addParameter("-json")

        if (config.isVerbose) commandLine.addParameter("-v")
        if (config.isShort)   commandLine.addParameter("-short")
        if (config.isRace)    commandLine.addParameter("-race")

        if (config.buildTags.isNotEmpty()) {
            commandLine.addParameter("-tags")
            commandLine.addParameter(config.buildTags)
        }

        // Coverage profile — only injected when launched via CoverageExecutor.
        // -covermode=count: record actual execution counts (not just 0/1 as in "set" mode).
        // -count=1: disable Go's test-result cache so every coverage run produces fresh
        //           counters; cached results replay old coverage data and ignore -covermode.
        coverageFilePath?.let {
            commandLine.addParameter("-coverprofile=$it")
            commandLine.addParameter("-covermode=count")
            commandLine.addParameter("-count=1")
        }

        // Extra go tool arguments from the settings UI
        if (config.goToolArguments.isNotBlank()) {
            ParametersListUtil.parse(config.goToolArguments).forEach { commandLine.addParameter(it) }
        }

        when (config.scope) {
            GoTestScope.DIRECTORY -> {
                commandLine.addParameter("./...")
                commandLine.withWorkDirectory(FileUtil.toSystemDependentName(config.targetPath))
            }
            GoTestScope.PACKAGE -> commandLine.addParameter(config.targetPath)
            GoTestScope.FILE    -> commandLine.addParameter(".")
            GoTestScope.FUNCTION -> {
                val parts = config.targetPath.split(":")
                if (parts.size == 2) {
                    commandLine.addParameter(parts[0])
                    commandLine.addParameter("-run")
                    commandLine.addParameter("^${parts[1]}$")
                } else {
                    commandLine.addParameter(config.targetPath)
                }
            }
        }

        // Program arguments passed to the test binary (after --)
        if (config.programArguments.isNotBlank()) {
            commandLine.addParameter("--")
            ParametersListUtil.parse(config.programArguments).forEach { commandLine.addParameter(it) }
        }

        commandLine.withWorkDirectory(FileUtil.toSystemDependentName(config.workingDirectory))
        commandLine.withEnvironment(config.envVariables)

        // Build the setup lines that appear at the top of the console (folded by GoConsoleFolding).
        val setupLines = buildList {
            if (sdkPath.isNotEmpty()) add("GOROOT=$sdkPath")
            add(commandLine.commandLineString)
        }

        // Wire the retranslator's result callback to GoTestResultsService so the
        // line marker contributor can pick up pass/fail status for gutter icons.
        val resultsService = GoTestResultsService.getInstance(config.project)
        val onTestResult: (String, Boolean?) -> Unit = { testName, passed ->
            val status = when (passed) {
                true  -> GoTestResultsService.Status.PASSED
                false -> GoTestResultsService.Status.FAILED
                null  -> GoTestResultsService.Status.SKIPPED
            }
            resultsService.record(testName, status)
        }

        val handler = GoTestProcessHandler(commandLine, setupLines, onTestResult)
        ProcessTerminatedListener.attach(handler)
        return handler
    }

    /**
     * Filters raw JSON stdout into TeamCity service messages for the SMTestRunner,
     * and emits foldable `#gosetup` lines before the process output begins.
     */
    private class GoTestProcessHandler(
        commandLine: GeneralCommandLine,
        private val setupLines: List<String>,
        onTestResult: (testName: String, passed: Boolean?) -> Unit
    ) : OSProcessHandler(commandLine) {

        private val retranslator = GoTestJsonRetranslator(onTestResult)

        // Suppresses the automatic command-line echo that OSProcessHandler emits in
        // startNotify() so we don't print it twice (we already emit it as a #gosetup line).
        @Volatile private var skipNextCommandEcho = false

        override fun startNotify() {
            // Emit each setup line before the process starts so they appear at the
            // very top of the console and the ConsoleFolding can collapse them.
            setupLines.forEach { line ->
                notifyTextAvailable("$line #gosetup\n", ProcessOutputTypes.SYSTEM)
            }
            skipNextCommandEcho = true
            super.startNotify()
        }

        override fun notifyTextAvailable(text: String, outputType: Key<*>) {
            // Drop the duplicate command echo from OSProcessHandler.startNotify().
            if (skipNextCommandEcho && outputType == ProcessOutputTypes.SYSTEM) {
                skipNextCommandEcho = false
                return
            }
            if (outputType == ProcessOutputTypes.STDOUT) {
                val translated = retranslator.translateLine(text)
                if (translated != null) super.notifyTextAvailable(translated, outputType)
            } else {
                super.notifyTextAvailable(text, outputType)
            }
        }
    }
}
