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

class GoModTidyBeforeRunTask(taskKey: Key<GoModTidyBeforeRunTask>) : BeforeRunTask<GoModTidyBeforeRunTask>(taskKey)

class GoModTidyBeforeRunTaskProvider : BeforeRunTaskProvider<GoModTidyBeforeRunTask>() {
    companion object {
        val ID: Key<GoModTidyBeforeRunTask> = Key.create("GoModTidyBeforeRunTask")
    }

    override fun getId(): Key<GoModTidyBeforeRunTask> = ID

    override fun getName(): String = "go mod tidy"

    override fun getIcon(): Icon? = null

    override fun createTask(runConfiguration: RunConfiguration): GoModTidyBeforeRunTask? {
        return if (runConfiguration is GoRunConfiguration) GoModTidyBeforeRunTask(ID) else null
    }

    override fun executeTask(
        context: DataContext,
        configuration: RunConfiguration,
        env: ExecutionEnvironment,
        task: GoModTidyBeforeRunTask
    ): Boolean {
        if (configuration !is GoRunConfiguration) return false

        val sdkPath = GoProjectSettings.getInstance(configuration.project).state.goSdkPath
        val goExe = GoSdkLocator.getGoExecutablePath(sdkPath) ?: return false

        val commandLine = GeneralCommandLine(goExe, "mod", "tidy")
            .withWorkDirectory(configuration.workingDirectory)
        
        val handler = CapturingProcessHandler(commandLine)
        val result = handler.runProcess(30000) // 30s timeout
        return result.exitCode == 0
    }
}
