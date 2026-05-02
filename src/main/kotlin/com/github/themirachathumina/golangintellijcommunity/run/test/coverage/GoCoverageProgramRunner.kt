package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.run.GO_COVERAGE_DIR_KEY
import com.github.themirachathumina.golangintellijcommunity.run.GoCompilationStatusWidget
import com.github.themirachathumina.golangintellijcommunity.run.GoRunConfiguration
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfiguration
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageExecutor
import com.intellij.coverage.CoverageRunnerData
import com.intellij.execution.configurations.ConfigurationInfoProvider
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.io.FileUtil
import java.util.concurrent.atomic.AtomicReference
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import java.io.File
import java.nio.file.Paths

/**
 * Program runner that enables "Run with Coverage" for Go test configurations.
 *
 * This implementation follows the Flutter plugin's pattern: it executes the
 * state, attaches a process listener, and upon termination, refreshes the
 * VFS for the coverage output file before triggering the [CoverageDataManager]
 * to process the gathered data.
 */
class GoCoverageProgramRunner : GenericProgramRunner<RunnerSettings>() {

    override fun getRunnerId(): String = "GoCoverageProgramRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        executorId == CoverageExecutor.EXECUTOR_ID &&
        (profile is GoTestRunConfiguration || profile is GoRunConfiguration)

    override fun createConfigurationData(settingsProvider: ConfigurationInfoProvider): RunnerSettings {
        return CoverageRunnerData()
    }

    override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
        val project = environment.project

        // For test coverage runs, go test handles compilation internally so we show the widget
        // for the full run duration. For GoRunConfiguration coverage, buildWithProgress already
        // shows its own "Compiling Go program with coverage..." widget during the build phase.
        val isTestRun = environment.runProfile is GoTestRunConfiguration
        val destroyProcess = AtomicReference<(() -> Unit)?>(null)

        if (isTestRun) {
            GoCompilationStatusWidget.install(project, "Running Go tests with coverage...") {
                destroyProcess.get()?.invoke()
                GoCompilationStatusWidget.remove(project)
            }
        }

        val executionResult = state.execute(environment.executor, this) ?: run {
            if (isTestRun) GoCompilationStatusWidget.remove(project)
            return null
        }
        val handler = executionResult.processHandler

        if (isTestRun) {
            destroyProcess.set { handler.destroyProcess() }
        }

        handler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
                if (isTestRun) GoCompilationStatusWidget.remove(project)
                ApplicationManager.getApplication().invokeLater {
                    processCoverage(environment)
                }
            }
        })

        return RunContentBuilder(executionResult, environment).showRunContent(environment.contentToReuse)
    }

    private fun processCoverage(env: ExecutionEnvironment) {
        when (val profile = env.runProfile) {
            is GoTestRunConfiguration -> processTestCoverage(env, profile)
            is GoRunConfiguration     -> processRunCoverage(env, profile)
        }
    }

    private fun processTestCoverage(env: ExecutionEnvironment, runConfig: GoTestRunConfiguration) {
        val coverageConfig = CoverageEnabledConfiguration.getOrCreate(runConfig)
        val pathStr = coverageConfig.coverageFilePath ?: return
        refreshAndProcess(env, runConfig, pathStr)
    }

    /**
     * For [GoRunConfiguration], the binary was built with `-cover` and ran with `GOCOVERDIR`
     * pointing to a temp directory. Convert that raw coverage data to the standard
     * `go test` coverprofile format using `go tool covdata textfmt`, then process normally.
     */
    private fun processRunCoverage(env: ExecutionEnvironment, runConfig: GoRunConfiguration) {
        val coverageDir  = env.getUserData(GO_COVERAGE_DIR_KEY) ?: return
        val coverageConfig = CoverageEnabledConfiguration.getOrCreate(runConfig)
        val pathStr = coverageConfig.coverageFilePath ?: return

        // Conversion is blocking — dispatch to a pooled thread so we don't block the EDT.
        ApplicationManager.getApplication().executeOnPooledThread {
            val sdkPath = GoProjectSettings.getInstance(runConfig.project).state.goSdkPath
            val goExe   = GoSdkUtils.getGoExecutablePath(sdkPath) ?: return@executeOnPooledThread
            val cmd = GeneralCommandLine(
                FileUtil.toSystemDependentName(goExe),
                "tool", "covdata", "textfmt",
                "-i=${FileUtil.toSystemDependentName(coverageDir)}",
                "-o=${FileUtil.toSystemDependentName(pathStr)}"
            )
            CapturingProcessHandler(cmd).runProcess()

            ApplicationManager.getApplication().invokeLater {
                refreshAndProcess(env, runConfig, pathStr)
            }
        }
    }

    private fun refreshAndProcess(
        env: ExecutionEnvironment,
        runConfig: com.intellij.execution.configurations.RunConfigurationBase<*>,
        pathStr: String
    ) {
        val path   = Paths.get(pathStr)
        val parent = path.parent ?: return

        // findFileByNioFile is a cache-only lookup — no disk I/O on EDT.
        val parentVFile = LocalFileSystem.getInstance().findFileByNioFile(parent)

        val afterRefresh = Runnable {
            if (File(pathStr).exists()) {
                val settings = env.runnerSettings
                if (settings != null) {
                    CoverageDataManager.getInstance(env.project)
                        .processGatheredCoverage(runConfig, settings)
                }
            }
        }

        if (parentVFile != null) {
            RefreshQueue.getInstance().refresh(true, true, afterRefresh, parentVFile)
        } else {
            ApplicationManager.getApplication().executeOnPooledThread {
                LocalFileSystem.getInstance().refreshAndFindFileByNioFile(parent)
                ApplicationManager.getApplication().invokeLater(afterRefresh)
            }
        }
    }
}
