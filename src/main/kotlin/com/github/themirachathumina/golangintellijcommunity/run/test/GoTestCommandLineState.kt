package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.execution.ParametersListUtil

class GoTestCommandLineState(
    env: ExecutionEnvironment,
    private val config: GoTestRunConfiguration
) : CommandLineState(env) {

    override fun startProcess(): ProcessHandler {
        val sdkPath = GoProjectSettings.getInstance(config.project).state.goSdkPath
        val goExe = GoSdkUtils.getGoExecutablePath(sdkPath)
            ?: throw com.intellij.execution.ExecutionException("Go executable not found in SDK path: $sdkPath")

        val commandLine = GeneralCommandLine(goExe)
        commandLine.addParameter("test")
        commandLine.addParameter("-json")

        if (config.isVerbose) {
            commandLine.addParameter("-v")
        }
        if (config.isShort) {
            commandLine.addParameter("-short")
        }
        if (config.isRace) {
            commandLine.addParameter("-race")
        }
        if (config.buildTags.isNotEmpty()) {
            commandLine.addParameter("-tags")
            commandLine.addParameter(config.buildTags)
        }

        // Extra go tool arguments from the UI
        if (config.goToolArguments.isNotBlank()) {
            ParametersListUtil.parse(config.goToolArguments).forEach { commandLine.addParameter(it) }
        }

        when (config.scope) {
            GoTestScope.DIRECTORY -> {
                commandLine.addParameter("./...")
                commandLine.withWorkDirectory(FileUtil.toSystemDependentName(config.targetPath))
            }
            GoTestScope.PACKAGE -> {
                commandLine.addParameter(config.targetPath)
            }
            GoTestScope.FILE -> {
                // Run the package (.) so all package dependencies are built correctly
                commandLine.addParameter(".")
            }
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

        val handler = GoTestProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        return handler
    }

    override fun execute(executor: Executor, runner: com.intellij.execution.runners.ProgramRunner<*>): ExecutionResult {
        val processHandler = startProcess()
        val consoleProperties = GoTestConsoleProperties(config, executor)
        consoleProperties.setProcessHandler(processHandler)

        val consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole("GoTest", processHandler, consoleProperties)

        return com.intellij.execution.DefaultExecutionResult(consoleView, processHandler)
    }

    /**
     * Custom ProcessHandler that filters out raw JSON and notifies translated TeamCity messages instead.
     */
    private class GoTestProcessHandler(commandLine: GeneralCommandLine) : OSProcessHandler(commandLine) {
        private val retranslator = GoTestJsonRetranslator()

        override fun notifyTextAvailable(text: String, outputType: Key<*>) {
            if (outputType == ProcessOutputTypes.STDOUT) {
                val translated = retranslator.translateLine(text)
                if (translated != null) {
                    super.notifyTextAvailable(translated, outputType)
                }
            } else {
                super.notifyTextAvailable(text, outputType)
            }
        }
    }
}
