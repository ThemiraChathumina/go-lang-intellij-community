package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import java.io.OutputStream

class GoRunProfileState(
    env: ExecutionEnvironment,
    private val config: GoRunConfiguration
) : CommandLineState(env) {

    override fun startProcess(): ProcessHandler {
        val sdkPath = com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings.getInstance(environment.project).state.goSdkPath
        val goExe = com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils.getGoExecutablePath(sdkPath)
            ?: throw ExecutionException("Go SDK is not configured for this project.")
        println("go exe: $goExe")
        // 1. Prepare temporary executable path
        val outputExeFile = FileUtil.createTempFile("go_build_", if (SystemInfo.isWindows) ".exe" else "", true)
        val outputExe = outputExeFile.absolutePath

        // 2. Compile step (go build)
        val buildCmd = GeneralCommandLine(goExe, "build", "-o", outputExe)
        buildCmd.parametersList.addParametersString(config.goToolArguments)
        buildCmd.addParameter(config.targetPath)
        buildCmd.withWorkDirectory(config.workingDirectory)
        buildCmd.environment.putAll(config.envVariables)

        if (sdkPath.isNotEmpty() && com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils.isValidSdkHome(sdkPath)) {
            buildCmd.withEnvironment("GOROOT", sdkPath)
            val oldPath = System.getenv("PATH") ?: ""
            val binPath = File(sdkPath, "bin").absolutePath
            buildCmd.withEnvironment("PATH", "$binPath${File.pathSeparator}$oldPath")
        }

        val buildProcess = CapturingProcessHandler(buildCmd)
        var buildOutput: ProcessOutput? = null

        // FIX: Run the build process safely using the ProgressManager so we don't block the UI thread
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                buildOutput = buildProcess.runProcess(30000)
            },
            "Building Go Application...",
            true, // Allow the user to cancel the build
            environment.project
        )

        val output = buildOutput

        // 3. Handle Compilation Failure or Cancellation
        if (output == null || output.exitCode != 0) {
            return object : ProcessHandler() {
                override fun destroyProcessImpl() = notifyProcessTerminated(output?.exitCode ?: -1)
                override fun detachProcessImpl() = notifyProcessTerminated(output?.exitCode ?: -1)
                override fun detachIsDefault(): Boolean = false
                override fun getProcessInput(): OutputStream? = null

                override fun startNotify() {
                    super.startNotify()
                    notifyTextAvailable("Build failed or was cancelled:\n", ProcessOutputTypes.SYSTEM)
                    if (output != null) {
                        notifyTextAvailable(output.stdout, ProcessOutputTypes.STDOUT)
                        notifyTextAvailable(output.stderr, ProcessOutputTypes.STDERR)
                    }
                    notifyProcessTerminated(output?.exitCode ?: -1)
                }
            }
        }

        // 4. Execution step (Run the compiled binary)
        val runCmd = GeneralCommandLine(outputExe)
        runCmd.parametersList.addParametersString(config.programArguments)
        runCmd.withWorkDirectory(config.workingDirectory)
        runCmd.environment.putAll(config.envVariables)

        // OSProcessHandler automatically wires the binary's stdout/stderr to the IntelliJ Run Console
        val processHandler = OSProcessHandler(runCmd)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }
}