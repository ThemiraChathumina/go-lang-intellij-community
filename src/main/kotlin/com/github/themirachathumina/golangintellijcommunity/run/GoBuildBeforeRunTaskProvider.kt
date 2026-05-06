package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.Key
import javax.swing.Icon

/*
 * BeforeRunTask and provider that trigger a Go build step before a run configuration executes.
 * The build result is stored in the ExecutionEnvironment for the run profile state to consume.
 */
class GoBuildBeforeRunTask(taskKey: Key<GoBuildBeforeRunTask>) : BeforeRunTask<GoBuildBeforeRunTask>(taskKey) {
    init { isEnabled = true }
}

class GoBuildBeforeRunTaskProvider : BeforeRunTaskProvider<GoBuildBeforeRunTask>() {
    companion object {
        val ID: Key<GoBuildBeforeRunTask> = Key.create("GoBuildBeforeRunTask")
    }

    override fun getId(): Key<GoBuildBeforeRunTask> = ID

    override fun getName(): String = "Go Build"

    override fun getIcon(): Icon? = null

    override fun createTask(runConfiguration: RunConfiguration): GoBuildBeforeRunTask? {
        if (runConfiguration !is GoRunConfiguration) return null
        return GoBuildBeforeRunTask(ID)
    }

    override fun executeTask(
        context: DataContext,
        configuration: RunConfiguration,
        env: ExecutionEnvironment,
        task: GoBuildBeforeRunTask
    ): Boolean {
        if (configuration !is GoRunConfiguration) return false

        val result = buildGoApplication(configuration)
        env.putUserData(GO_BUILD_RESULT_KEY, result)

        return true
    }
}
