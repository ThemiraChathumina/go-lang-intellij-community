package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import java.io.File
import javax.swing.Icon

data class GoBuildResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val exePath: String,
    val buildCmdString: String,
    val goRoot: String,
    val goPath: String
)

val GO_BUILD_RESULT_KEY: Key<GoBuildResult> = Key.create("GoBuildResult")

/**
 * Builds a Go application from the given run configuration.
 * Can be called from any thread except EDT.
 */
fun buildGoApplication(config: GoRunConfiguration): GoBuildResult {
    val sdkPath = GoProjectSettings.getInstance(config.project).state.goSdkPath
    val goExe = GoSdkUtils.getGoExecutablePath(sdkPath)
        ?: throw ExecutionException("Go SDK is not configured for this project.")

    val outputExe = if (config.outputPath.isNotEmpty()) {
        config.outputPath
    } else {
        FileUtil.createTempFile("go_build_", if (SystemInfo.isWindows) ".exe" else "", true).absolutePath
    }

    val buildCmd = GeneralCommandLine(goExe, "build", "-o", outputExe)

    if (config.goToolArguments.isNotEmpty()) {
        buildCmd.parametersList.addParametersString(config.goToolArguments)
    }
    if (config.enableRace) {
        buildCmd.addParameter("-race")
    }
    if (config.buildTags.isNotEmpty()) {
        buildCmd.addParameters("-tags", config.buildTags)
    }
    val gcFlags = when (config.buildMode) {
        GoBuildMode.DEBUG -> "all=-N -l"
        else -> ""
    }
    if (gcFlags.isNotEmpty()) {
        buildCmd.addParameter("-gcflags=$gcFlags")
    }

    buildCmd.addParameter(config.targetPath)
    buildCmd.withWorkDirectory(config.workingDirectory)

    val envVars = buildCmd.environment
    envVars.putAll(config.envVariables)
    if (config.goOS.isNotEmpty()) envVars["GOOS"] = config.goOS
    if (config.goArch.isNotEmpty()) envVars["GOARCH"] = config.goArch
    envVars["CGO_ENABLED"] = if (config.cgoEnabled) "1" else "0"
    if (config.goProxy.isNotEmpty()) envVars["GOPROXY"] = config.goProxy

    if (sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)) {
        envVars["GOROOT"] = sdkPath
        val oldPath = System.getenv("PATH") ?: ""
        val binPath = File(sdkPath, "bin").absolutePath
        envVars["PATH"] = "$binPath${File.pathSeparator}$oldPath"
    }

    val goPath = envVars["GOPATH"] ?: System.getenv("GOPATH") ?: ""

    val handler = CapturingProcessHandler(buildCmd)
    val result = handler.runProcess()

    return GoBuildResult(
        exitCode = result.exitCode,
        stdout = result.stdout,
        stderr = result.stderr,
        exePath = outputExe,
        buildCmdString = buildCmd.commandLineString,
        goRoot = sdkPath,
        goPath = goPath
    )
}

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

        // Always return true so the console opens (even for build errors)
        return true
    }
}
