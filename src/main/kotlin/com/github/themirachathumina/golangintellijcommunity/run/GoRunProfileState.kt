package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Ref
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.File
import java.io.OutputStream
import java.util.concurrent.TimeUnit

private fun toNative(path: String): String = FileUtil.toSystemDependentName(path)

class GoRunProfileState(
    env: ExecutionEnvironment,
    private val config: GoRunConfiguration
) : CommandLineState(env) {

    @Volatile private var coverageDir: String? = null

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        if (executor is CoverageExecutor) {
            // Initialise the coverage config early so the suite is registered before the process starts.
            CoverageEnabledConfiguration.getOrCreate(config)
            val tempDir = FileUtil.createTempDirectory("go_covdir_", null, true)
            coverageDir = tempDir.absolutePath
            environment.putUserData(GO_COVERAGE_DIR_KEY, tempDir.absolutePath)
        }
        return super.execute(executor, runner)
    }

    override fun startProcess(): ProcessHandler {
        // For coverage runs, always rebuild with -cover instrumentation.
        // Otherwise use the before-run task result or build now.
        val buildResult = if (coverageDir != null) buildWithProgress(withCoverage = true)
                          else environment.getUserData(GO_BUILD_RESULT_KEY) ?: buildWithProgress()

        val outputExe = buildResult.exePath

        // Prepare run command
        val runCmd = if (config.runInExternalTerminal) {
            createExternalTerminalCommandLine(outputExe, config)
        } else {
            val cmd = GeneralCommandLine(outputExe)
            if (config.programArguments.isNotEmpty()) {
                cmd.parametersList.addParametersString(config.programArguments)
            }
            cmd
        }

        runCmd.withWorkDirectory(toNative(config.workingDirectory))

        // Forward environment variables to the run command
        val envVars = runCmd.environment
        envVars.putAll(config.envVariables)
        // Coverage: tell the instrumented binary where to write coverage data
        coverageDir?.let { envVars["GOCOVERDIR"] = FileUtil.toSystemDependentName(it) }
        if (config.goOS.isNotEmpty()) envVars["GOOS"] = config.goOS
        if (config.goArch.isNotEmpty()) envVars["GOARCH"] = config.goArch
        envVars["CGO_ENABLED"] = if (config.cgoEnabled) "1" else "0"
        if (config.goProxy.isNotEmpty()) envVars["GOPROXY"] = config.goProxy

        val sdkPath = buildResult.goRoot
        if (sdkPath.isNotEmpty()) {
            envVars["GOROOT"] = sdkPath
            val oldPath = System.getenv("PATH") ?: ""
            val binPath = File(sdkPath, "bin").absolutePath
            envVars["PATH"] = "$binPath${File.pathSeparator}$oldPath"
        }

        // Redirect stdin
        if (config.stdinFilePath.isNotEmpty()) {
            val stdinFile = File(toNative(config.stdinFilePath))
            if (stdinFile.exists()) {
                runCmd.withInput(stdinFile)
            }
        }

        return GoRunProcessHandler(buildResult, runCmd, config.timeoutSeconds)
    }

    private fun buildWithProgress(withCoverage: Boolean = false): GoBuildResult {
        val project = environment.project
        val message = if (withCoverage) "Compiling Go program with coverage..." else "Compiling Go program..."

        // On EDT (e.g. coverage runs) use runProcessWithProgressSynchronously which pumps the event
        // loop safely. Status bar widget is also installed so cancel works from there too.
        if (ApplicationManager.getApplication().isDispatchThread) {
            val cancelled = AtomicBoolean(false)
            val destroyBuild = AtomicReference<(() -> Unit)?>(null)
            val resultRef = Ref<GoBuildResult>()
            val errorRef = Ref<Exception>()

            GoCompilationStatusWidget.install(project, message) {
                cancelled.set(true)
                destroyBuild.get()?.invoke()
            }

            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    try {
                        resultRef.set(buildGoApplication(config, withCoverage = withCoverage) { handler ->
                            destroyBuild.set { handler.destroyProcess() }
                            if (cancelled.get()) handler.destroyProcess()
                        })
                    } catch (e: Exception) {
                        errorRef.set(e)
                    }
                },
                message, true, project
            )

            GoCompilationStatusWidget.remove(project)
            errorRef.get()?.let { throw ExecutionException(it.message, it) }
            return resultRef.get() ?: throw ExecutionException("Build was cancelled.")
        }

        // Already on a background thread — show status bar widget only, no dialog.
        val cancelled = AtomicBoolean(false)
        val destroyBuild = AtomicReference<(() -> Unit)?>(null)

        GoCompilationStatusWidget.install(project, message) {
            cancelled.set(true)
            destroyBuild.get()?.invoke()
        }

        try {
            val result = buildGoApplication(config, withCoverage = withCoverage) { handler ->
                destroyBuild.set { handler.destroyProcess() }
                if (cancelled.get()) handler.destroyProcess()
            }
            if (cancelled.get()) throw ExecutionException("Build was cancelled.")
            return result
        } catch (e: ExecutionException) {
            throw e
        } catch (e: Exception) {
            if (cancelled.get()) throw ExecutionException("Build was cancelled.")
            throw ExecutionException(e.message, e)
        } finally {
            GoCompilationStatusWidget.remove(project)
        }
    }

    private fun createExternalTerminalCommandLine(exePath: String, config: GoRunConfiguration): GeneralCommandLine {
        return if (SystemInfo.isWindows) {
            val tempBat = FileUtil.createTempFile("go_run_", ".bat", true)
            val batContent = """
                @echo off
                "$exePath" ${config.programArguments}
                if %errorlevel% neq 0 (
                    echo.
                    echo Process finished with exit code %errorlevel%
                )
                pause
            """.trimIndent()
            FileUtil.writeToFile(tempBat, batContent)

            GeneralCommandLine("cmd.exe", "/c", "start", "/wait", tempBat.absolutePath)
        } else if (SystemInfo.isMac) {
            GeneralCommandLine("open", "-a", "Terminal", exePath)
        } else {
            GeneralCommandLine("x-terminal-emulator", "-e", "\"$exePath\" ${config.programArguments}")
        }
    }
}

/**
 * A ProcessHandler that displays a foldable setup block, then runs the application.
 * The build has already completed before this handler is created.
 */
class GoRunProcessHandler(
    private val buildResult: GoBuildResult,
    private val runCmd: GeneralCommandLine,
    private val timeoutSeconds: Int
) : ProcessHandler() {

    private var runProcessHandler: OSProcessHandler? = null
    private var isDestroyed = false

    override fun startNotify() {
        super.startNotify()

        if (buildResult.exitCode == 0) {
            emitSetupBlock(true)
            runApplication()
        } else {
            emitSetupBlock(false)
            val stderr = buildResult.stderr.trim()
            if (stderr.isNotEmpty()) {
                notifyTextAvailable(stderr + "\n", ProcessOutputTypes.STDERR)
            }
            val stdout = buildResult.stdout.trim()
            if (stdout.isNotEmpty()) {
                notifyTextAvailable(stdout + "\n", ProcessOutputTypes.STDERR)
            }
            notifyTextAvailable("Process finished with exit code ${buildResult.exitCode}\n", ProcessOutputTypes.SYSTEM)
            notifyProcessTerminated(buildResult.exitCode)
        }
    }

    private fun emitSetupBlock(buildSucceeded: Boolean) {
        notifyTextAvailable("GOROOT=${buildResult.goRoot} #gosetup\n", ProcessOutputTypes.SYSTEM)
        notifyTextAvailable("GOPATH=${buildResult.goPath} #gosetup\n", ProcessOutputTypes.SYSTEM)
        notifyTextAvailable("${buildResult.buildCmdString} #gosetup\n", ProcessOutputTypes.SYSTEM)
        if (buildSucceeded) {
            notifyTextAvailable("${runCmd.commandLineString} #gosetup\n", ProcessOutputTypes.SYSTEM)
        }
    }

    private fun runApplication() {
        if (isDestroyed) return

        val handler = OSProcessHandler(runCmd)
        runProcessHandler = handler

        handler.addProcessListener(object : ProcessAdapter() {
            private var firstLineSkipped = false

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                val text = event.text
                if (text.isEmpty()) return

                // Skip the initial command line echoed by OSProcessHandler
                if (!firstLineSkipped && text.endsWith("\n")) {
                    firstLineSkipped = true
                    return
                }

                notifyTextAvailable(text, outputType)
            }

            override fun processTerminated(event: ProcessEvent) {
                notifyTextAvailable("Process finished with exit code ${event.exitCode}\n", ProcessOutputTypes.SYSTEM)
                notifyProcessTerminated(event.exitCode)
            }
        })

        if (timeoutSeconds > 0) {
            AppExecutorUtil.getAppScheduledExecutorService().schedule({
                if (!handler.isProcessTerminated) {
                    notifyTextAvailable("\nProcess timed out after $timeoutSeconds seconds. Killing...\n", ProcessOutputTypes.STDERR)
                    handler.destroyProcess()
                }
            }, timeoutSeconds.toLong(), TimeUnit.SECONDS)
        }

        handler.startNotify()
    }

    override fun destroyProcessImpl() {
        isDestroyed = true
        runProcessHandler?.destroyProcess()
    }

    override fun detachProcessImpl() {
        isDestroyed = true
        runProcessHandler?.detachProcess()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = runProcessHandler?.processInput
}
