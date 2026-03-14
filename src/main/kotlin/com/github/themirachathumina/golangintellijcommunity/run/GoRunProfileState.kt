package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.File
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class GoRunProfileState(
    env: ExecutionEnvironment,
    private val config: GoRunConfiguration
) : CommandLineState(env) {

    override fun startProcess(): ProcessHandler {
        val sdkPath = GoProjectSettings.getInstance(environment.project).state.goSdkPath
        val goExe = GoSdkUtils.getGoExecutablePath(sdkPath)
            ?: throw ExecutionException("Go SDK is not configured for this project.")

        // 1. Prepare output executable path
        val outputExe = if (config.outputPath.isNotEmpty()) {
            config.outputPath
        } else {
            FileUtil.createTempFile("go_build_", if (SystemInfo.isWindows) ".exe" else "", true).absolutePath
        }

        // 2. Prepare build command
        val buildCmd = GeneralCommandLine(goExe, "build", "-o", outputExe)
        
        // Apply Go tool arguments
        if (config.goToolArguments.isNotEmpty()) {
            buildCmd.parametersList.addParametersString(config.goToolArguments)
        }
        
        // Apply race detector
        if (config.enableRace) {
            buildCmd.addParameter("-race")
        }
        
        // Apply build tags
        if (config.buildTags.isNotEmpty()) {
            buildCmd.addParameters("-tags", config.buildTags)
        }
        
        // Apply gcflags based on build mode
        val gcFlags = when (config.buildMode) {
            GoBuildMode.DEBUG -> "all=-N -l"
            else -> ""
        }
        if (gcFlags.isNotEmpty()) {
            buildCmd.addParameter("-gcflags=$gcFlags")
        }

        buildCmd.addParameter(config.targetPath)
        buildCmd.withWorkDirectory(config.workingDirectory)
        
        // Set environment variables for build
        val envVars = buildCmd.environment
        envVars.putAll(config.envVariables)
        if (config.goOS.isNotEmpty()) envVars["GOOS"] = config.goOS
        if (config.goArch.isNotEmpty()) envVars["GOARCH"] = config.goArch
        envVars["CGO_ENABLED"] = if (config.cgoEnabled) "1" else "0"
        if (config.goProxy.isNotEmpty()) envVars["GOPROXY"] = config.goProxy
        
        // Forward GOROOT and PATH
        if (sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)) {
            envVars["GOROOT"] = sdkPath
            val oldPath = System.getenv("PATH") ?: ""
            val binPath = File(sdkPath, "bin").absolutePath
            envVars["PATH"] = "$binPath${File.pathSeparator}$oldPath"
        }

        // 3. Prepare run command
        val runCmd = if (config.runInExternalTerminal) {
            createExternalTerminalCommandLine(outputExe, config)
        } else {
            val cmd = GeneralCommandLine(outputExe)
            if (config.programArguments.isNotEmpty()) {
                cmd.parametersList.addParametersString(config.programArguments)
            }
            cmd
        }
        
        runCmd.withWorkDirectory(config.workingDirectory)
        
        // Forward all build environments to the run command as well
        runCmd.environment.putAll(buildCmd.environment)
        
        // Redirect stdin
        if (config.stdinFilePath.isNotEmpty()) {
            val stdinFile = File(config.stdinFilePath)
            if (stdinFile.exists()) {
                runCmd.withInput(stdinFile)
            }
        }

        // 4. Return the sequential process handler that handles build then run
        return GoRunProcessHandler(buildCmd, runCmd, config.timeoutSeconds)
    }

    private fun createExternalTerminalCommandLine(exePath: String, config: GoRunConfiguration): GeneralCommandLine {
        return if (SystemInfo.isWindows) {
            // To avoid complex nested quoting issues with cmd.exe /c and GeneralCommandLine's own escaping,
            // we create a temporary batch file that launches the application and pauses.
            val tempBat = FileUtil.createTempFile("go_run_", ".bat", true)
            val batContent = """
                @echo off
                "$exePath" ${config.programArguments}
                if %errorlevel% neq 0 (
                    echo.
                    echo Process finished with exit code %errorlevel%
                )
                pause
            """.trimIndent()
            FileUtil.writeToFile(tempBat, batContent)
            
            GeneralCommandLine("cmd.exe", "/c", "start", "/wait", tempBat.absolutePath)
        } else if (SystemInfo.isMac) {
            GeneralCommandLine("open", "-a", "Terminal", exePath)
        } else {
            GeneralCommandLine("x-terminal-emulator", "-e", "\"$exePath\" ${config.programArguments}")
        }
    }
}

/**
 * A ProcessHandler that runs a build command followed by a run command.
 * It streams the output of both to the attached console.
 */
class GoRunProcessHandler(
    private val buildCmd: GeneralCommandLine,
    private val runCmd: GeneralCommandLine,
    private val timeoutSeconds: Int
) : ProcessHandler() {

    private var activeHandler: ProcessHandler? = null
    private var isDestroyed = false

    override fun startNotify() {
        super.startNotify()
        runBuild()
    }

    private fun runBuild() {
        if (isDestroyed) return
        
        notifyTextAvailable("Building Go Application:\n", ProcessOutputTypes.SYSTEM)
        notifyTextAvailable("  ${buildCmd.commandLineString}\n", ProcessOutputTypes.SYSTEM)
        
        val buildHandler = OSProcessHandler(buildCmd)
        activeHandler = buildHandler
        
        buildHandler.addProcessListener(object : ProcessAdapter() {
            private var lastLineFinished = true
            private var firstLineSkipped = false
            
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                val text = event.text
                if (text.isEmpty()) return
                
                // OSProcessHandler typically sends the command line as the first text event.
                // We skip it because we already printed it manually (and better formatted).
                if (!firstLineSkipped && text.endsWith("\n")) {
                    firstLineSkipped = true
                    return
                }
                
                // Indent build output to mark it as part of build details
                val output = if (lastLineFinished) "  " + text else text
                lastLineFinished = text.endsWith("\n")
                
                notifyTextAvailable(output, ProcessOutputTypes.SYSTEM)
            }

            override fun processTerminated(event: ProcessEvent) {
                if (event.exitCode == 0) {
                    runApplication()
                } else {
                    notifyTextAvailable("Build finished with exit code ${event.exitCode}\n", ProcessOutputTypes.SYSTEM)
                    notifyProcessTerminated(event.exitCode)
                }
            }
        })
        
        buildHandler.startNotify()
    }

    private fun runApplication() {
        if (isDestroyed) return
        
        notifyTextAvailable("Running Application:\n", ProcessOutputTypes.SYSTEM)
        notifyTextAvailable("  ${runCmd.commandLineString}\n", ProcessOutputTypes.SYSTEM)
        
        val runHandler = OSProcessHandler(runCmd)
        activeHandler = runHandler
        
        runHandler.addProcessListener(object : ProcessAdapter() {
            private var firstLineSkipped = false

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                val text = event.text
                if (text.isEmpty()) return

                // Skip the initial command line output from OSProcessHandler
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
                if (!runHandler.isProcessTerminated) {
                    notifyTextAvailable("\nProcess timed out after $timeoutSeconds seconds. Killing...\n", ProcessOutputTypes.STDERR)
                    runHandler.destroyProcess()
                }
            }, timeoutSeconds.toLong(), TimeUnit.SECONDS)
        }
        
        runHandler.startNotify()
    }

    override fun destroyProcessImpl() {
        isDestroyed = true
        activeHandler?.destroyProcess()
    }

    override fun detachProcessImpl() {
        isDestroyed = true
        activeHandler?.detachProcess()
    }

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = activeHandler?.processInput
}
