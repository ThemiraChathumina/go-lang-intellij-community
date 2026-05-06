package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private fun toNative(path: String): String = FileUtil.toSystemDependentName(path)

/*
 * CommandLineState that prepares and starts the Go run process. Handles coverage-instrumented builds,
 * environment variable injection, stdin redirection, and external terminal launches.
 */
class GoRunProfileState(
    env: ExecutionEnvironment,
    private val config: GoRunConfiguration
) : CommandLineState(env) {

    @Volatile private var coverageDir: String? = null

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        if (executor is CoverageExecutor) {
            CoverageEnabledConfiguration.getOrCreate(config)
            val tempDir = FileUtil.createTempDirectory("go_covdir_", null, true)
            coverageDir = tempDir.absolutePath
            environment.putUserData(GO_COVERAGE_DIR_KEY, tempDir.absolutePath)
        }
        return super.execute(executor, runner)
    }

    override fun startProcess(): ProcessHandler {
        val buildResult = if (coverageDir != null) buildWithProgress(withCoverage = true)
                          else environment.getUserData(GO_BUILD_RESULT_KEY) ?: buildWithProgress()

        val outputExe = buildResult.exePath

        val runCmd = if (config.runInExternalTerminal) {
            GoExternalTerminalLauncher.create(outputExe, config)
        } else {
            val cmd = GeneralCommandLine(outputExe)
            if (config.programArguments.isNotEmpty()) {
                cmd.parametersList.addParametersString(config.programArguments)
            }
            cmd
        }

        runCmd.withWorkDirectory(toNative(config.workingDirectory))

        val envVars = runCmd.environment
        envVars.putAll(config.envVariables)
        coverageDir?.let { envVars[GoConstants.Env.GOCOVERDIR] = FileUtil.toSystemDependentName(it) }
        if (config.goOS.isNotEmpty()) envVars[GoConstants.Env.GOOS] = config.goOS
        if (config.goArch.isNotEmpty()) envVars[GoConstants.Env.GOARCH] = config.goArch
        envVars[GoConstants.Env.CGO_ENABLED] = if (config.cgoEnabled) "1" else "0"
        if (config.goProxy.isNotEmpty()) envVars[GoConstants.Env.GOPROXY] = config.goProxy

        val sdkPath = buildResult.goRoot
        if (sdkPath.isNotEmpty()) {
            envVars[GoConstants.Env.GOROOT] = sdkPath
            val oldPath = System.getenv("PATH") ?: ""
            val binPath = File(sdkPath, "bin").absolutePath
            envVars["PATH"] = "$binPath${File.pathSeparator}$oldPath"
        }

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
}
