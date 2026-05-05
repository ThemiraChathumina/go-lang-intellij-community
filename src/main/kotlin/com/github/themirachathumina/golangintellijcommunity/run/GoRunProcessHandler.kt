package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.openapi.util.Key
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.OutputStream
import java.util.concurrent.TimeUnit

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
        notifyTextAvailable("${GoConstants.Env.GOROOT}=${buildResult.goRoot} #gosetup\n", ProcessOutputTypes.SYSTEM)
        notifyTextAvailable("${GoConstants.Env.GOPATH}=${buildResult.goPath} #gosetup\n", ProcessOutputTypes.SYSTEM)
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
