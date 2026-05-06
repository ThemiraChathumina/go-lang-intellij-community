package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.run.GoCompilationStatusWidget
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import java.util.concurrent.atomic.AtomicReference

/*
 * Async program runner for Go test configurations. Executes Go tests using 'go test' 
 * and handles the compilation status widget, process termination, and results display 
 * in the IDE's test runner UI.
 */
class GoTestProgramRunner : AsyncProgramRunner<RunnerSettings>() {

    override fun getRunnerId(): String = "GoTestProgramRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        profile is GoTestRunConfiguration && executorId == DefaultRunExecutor.EXECUTOR_ID

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        val promise = AsyncPromise<RunContentDescriptor?>()
        val project = environment.project
        val config = environment.runProfile as GoTestRunConfiguration

        FileDocumentManager.getInstance().saveAllDocuments()

        val message = when (config.testKind) {
            GoTestKind.BENCHMARK -> "Running Go benchmarks..."
            GoTestKind.FUZZ      -> "Running Go fuzz tests..."
            else                 -> "Running Go tests..."
        }

        val destroyProcess = AtomicReference<(() -> Unit)?>(null)

        GoCompilationStatusWidget.install(project, message) {
            destroyProcess.get()?.invoke()
            GoCompilationStatusWidget.remove(project)
        }

        // state.execute() creates the console UI and starts the process — must be on EDT.
        ApplicationManager.getApplication().invokeLater {
            try {
                val result = state.execute(environment.executor, this@GoTestProgramRunner)
                if (result != null) {
                    val handler = result.processHandler
                    destroyProcess.set { handler.destroyProcess() }

                    // Remove the widget when the test process finishes.
                    handler.addProcessListener(object : ProcessAdapter() {
                        override fun processTerminated(event: ProcessEvent) {
                            GoCompilationStatusWidget.remove(project)
                        }
                    })

                    val descriptor = RunContentBuilder(result, environment)
                        .showRunContent(environment.contentToReuse)
                    promise.setResult(descriptor)
                } else {
                    GoCompilationStatusWidget.remove(project)
                    promise.setResult(null)
                }
            } catch (e: Exception) {
                GoCompilationStatusWidget.remove(project)
                promise.setError(e)
            }
        }

        return promise
    }
}
