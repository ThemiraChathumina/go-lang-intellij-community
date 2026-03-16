package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise

class GoProgramRunner : AsyncProgramRunner<RunnerSettings>() {

    override fun getRunnerId(): String = "GoProgramRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        profile is GoRunConfiguration && executorId == DefaultRunExecutor.EXECUTOR_ID

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        val promise = AsyncPromise<RunContentDescriptor?>()
        val config = environment.runProfile as GoRunConfiguration

        // Flush unsaved editor changes to disk so go build compiles the latest code
        FileDocumentManager.getInstance().saveAllDocuments()

        ProgressManager.getInstance().run(object : Task.Backgroundable(
            environment.project, "Compiling Go program...", true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val buildResult = buildGoApplication(config)
                environment.putUserData(GO_BUILD_RESULT_KEY, buildResult)
            }

            override fun onSuccess() {
                try {
                    val result = state.execute(environment.executor, this@GoProgramRunner)
                    if (result != null) {
                        val descriptor = RunContentBuilder(result, environment)
                            .showRunContent(environment.contentToReuse)
                        promise.setResult(descriptor)
                    } else {
                        promise.setResult(null)
                    }
                } catch (e: Exception) {
                    promise.setError(e)
                }
            }

            override fun onCancel() {
                promise.setResult(null)
            }

            override fun onThrowable(error: Throwable) {
                promise.setError(error)
            }
        })

        return promise
    }
}
