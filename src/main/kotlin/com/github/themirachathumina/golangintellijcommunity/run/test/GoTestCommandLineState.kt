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

    private var coverageFilePath: String? = null

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
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

        coverageFilePath?.let {
            commandLine.addParameter("-coverprofile=$it")
            commandLine.addParameter("-covermode=count")
            commandLine.addParameter("-count=1")
        }

        if (config.goToolArguments.isNotBlank()) {
            ParametersListUtil.parse(config.goToolArguments).forEach { commandLine.addParameter(it) }
        }

        when (config.scope) {
            GoTestScope.DIRECTORY -> {
                commandLine.addParameter("./...")
                commandLine.withWorkDirectory(FileUtil.toSystemDependentName(config.targetPath))
                if (config.testKind == GoTestKind.BENCHMARK) {
                    commandLine.addParameter("-run=^$")
                    commandLine.addParameter("-bench=.")
                } else if (config.testKind == GoTestKind.FUZZ) {
                    commandLine.addParameter("-run=^$")
                    commandLine.addParameter("-fuzz=.")
                }
            }
            GoTestScope.PACKAGE -> {
                commandLine.addParameter(config.targetPath)
                if (config.testKind == GoTestKind.BENCHMARK) {
                    commandLine.addParameter("-run=^$")
                    commandLine.addParameter("-bench=.")
                } else if (config.testKind == GoTestKind.FUZZ) {
                    commandLine.addParameter("-run=^$")
                    commandLine.addParameter("-fuzz=.")
                }
            }
            GoTestScope.FILE -> {
                commandLine.addParameter(".")
                if (config.testKind == GoTestKind.BENCHMARK) {
                    commandLine.addParameter("-run=^$")
                    commandLine.addParameter("-bench=.")
                } else if (config.testKind == GoTestKind.FUZZ) {
                    commandLine.addParameter("-run=^$")
                    commandLine.addParameter("-fuzz=.")
                }
            }
            GoTestScope.FUNCTION -> {
                val parts = config.targetPath.split(":")
                if (parts.size == 2) {
                    commandLine.addParameter(parts[0])
                    val funcName = parts[1]

                    when (config.testKind) {
                        GoTestKind.BENCHMARK -> {
                            commandLine.addParameter("-run=^$")
                            commandLine.addParameter("-bench=^$funcName$")
                        }
                        GoTestKind.FUZZ -> {
                            commandLine.addParameter("-run=^$")
                            commandLine.addParameter("-fuzz=^$funcName$")
                        }
                        GoTestKind.TEST -> {
                            commandLine.addParameter("-run=^$funcName$")
                        }
                    }
                } else {
                    commandLine.addParameter(config.targetPath)
                    if (config.testKind == GoTestKind.BENCHMARK) {
                        commandLine.addParameter("-run=^$")
                        commandLine.addParameter("-bench=.")
                    } else if (config.testKind == GoTestKind.FUZZ) {
                        commandLine.addParameter("-run=^$")
                        commandLine.addParameter("-fuzz=.")
                    }
                }
            }
        }

        if (config.programArguments.isNotBlank()) {
            commandLine.addParameter("--")
            ParametersListUtil.parse(config.programArguments).forEach { commandLine.addParameter(it) }
        }

        commandLine.withWorkDirectory(FileUtil.toSystemDependentName(config.workingDirectory))
        commandLine.withEnvironment(config.envVariables)

        val setupLines = buildList {
            if (sdkPath.isNotEmpty()) add("GOROOT=$sdkPath")
            add(commandLine.commandLineString)
        }

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

    private class GoTestProcessHandler(
        commandLine: GeneralCommandLine,
        private val setupLines: List<String>,
        onTestResult: (testName: String, passed: Boolean?) -> Unit
    ) : OSProcessHandler(commandLine) {

        private val retranslator = GoTestJsonRetranslator(onTestResult)

        @Volatile private var skipNextCommandEcho = false

        override fun startNotify() {
            setupLines.forEach { line ->
                notifyTextAvailable("$line #gosetup\n", ProcessOutputTypes.SYSTEM)
            }
            skipNextCommandEcho = true
            super.startNotify()
        }

        override fun notifyTextAvailable(text: String, outputType: Key<*>) {
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