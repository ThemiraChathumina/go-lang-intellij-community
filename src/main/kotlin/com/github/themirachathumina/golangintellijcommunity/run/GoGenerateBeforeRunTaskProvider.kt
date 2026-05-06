package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkLocator
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.Key
import javax.swing.Icon

/*
 * BeforeRunTask and provider that run `go generate ./...` before a Go run configuration executes.
 */
class GoGenerateBeforeRunTask(taskKey: Key<GoGenerateBeforeRunTask>) : BeforeRunTask<GoGenerateBeforeRunTask>(taskKey)

class GoGenerateBeforeRunTaskProvider : BeforeRunTaskProvider<GoGenerateBeforeRunTask>() {
    companion object {
        val ID: Key<GoGenerateBeforeRunTask> = Key.create("GoGenerateBeforeRunTask")
    }

    override fun getId(): Key<GoGenerateBeforeRunTask> = ID

    override fun getName(): String = "go generate ./..."

    override fun getIcon(): Icon? = null

    override fun createTask(runConfiguration: RunConfiguration): GoGenerateBeforeRunTask? {
        return if (runConfiguration is GoRunConfiguration) GoGenerateBeforeRunTask(ID) else null
    }

    override fun executeTask(
        context: DataContext,
        configuration: RunConfiguration,
        env: ExecutionEnvironment,
        task: GoGenerateBeforeRunTask
    ): Boolean {
        if (configuration !is GoRunConfiguration) return false

        val sdkPath = GoProjectSettings.getInstance(configuration.project).state.goSdkPath
        val goExe = GoSdkLocator.getGoExecutablePath(sdkPath) ?: return false

        val commandLine = GeneralCommandLine(goExe, "generate", "./...")
            .withWorkDirectory(configuration.workingDirectory)
        
        val handler = CapturingProcessHandler(commandLine)
        val result = handler.runProcess(30000) // 30s timeout
        return result.exitCode == 0
    }
}
