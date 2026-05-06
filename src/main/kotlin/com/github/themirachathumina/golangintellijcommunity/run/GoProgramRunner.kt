package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/*
 * Async program runner for Go run configurations. Compiles the program on a background thread,
 * shows a cancellable status widget, and hands the built binary to the run profile state.
 */
class GoProgramRunner : AsyncProgramRunner<RunnerSettings>() {

    override fun getRunnerId(): String = "GoProgramRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        profile is GoRunConfiguration && executorId == DefaultRunExecutor.EXECUTOR_ID

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        val promise = AsyncPromise<RunContentDescriptor?>()
        val config = environment.runProfile as GoRunConfiguration
        val project = environment.project

        FileDocumentManager.getInstance().saveAllDocuments()

        val cancelled = AtomicBoolean(false)
        val destroyBuild = AtomicReference<(() -> Unit)?>(null)

        GoCompilationStatusWidget.install(project, "Compiling Go program...") {
            cancelled.set(true)
            destroyBuild.get()?.invoke()
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val buildResult = buildGoApplication(config) { handler ->
                    destroyBuild.set { handler.destroyProcess() }
                    // handle cancel arriving before handler was registered
                    if (cancelled.get()) handler.destroyProcess()
                }

                GoCompilationStatusWidget.remove(project)

                if (cancelled.get()) {
                    promise.setResult(null)
                    return@executeOnPooledThread
                }

                environment.putUserData(GO_BUILD_RESULT_KEY, buildResult)

                ApplicationManager.getApplication().invokeLater {
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
            } catch (e: Exception) {
                GoCompilationStatusWidget.remove(project)
                if (cancelled.get()) promise.setResult(null) else promise.setError(e)
            }
        }

        return promise
    }
}
