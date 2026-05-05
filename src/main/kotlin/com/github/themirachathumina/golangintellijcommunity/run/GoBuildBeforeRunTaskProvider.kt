package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.GoConstants
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
val GO_COVERAGE_DIR_KEY: Key<String> = Key.create("GoCoverageDir")

/**
 * Builds a Go application from the given run configuration.
 * Can be called from any thread except EDT.
 */
/**
 * Derives the binary name that `go build` produces when given a directory as -o target.
 * For FILE kind: uses the parent directory name of the target .go file.
 * For PACKAGE kind: uses the last segment of the target directory.
 */
private fun deriveBinaryName(config: GoRunConfiguration): String {
    val base = when (config.runKind) {
        GoRunKind.FILE -> {
            val targetFile = File(FileUtil.toSystemDependentName(config.targetPath))
            targetFile.parentFile?.name ?: "main"
        }
        GoRunKind.PACKAGE -> {
            File(FileUtil.toSystemDependentName(config.targetPath)).name
        }
    }
    return if (SystemInfo.isWindows) "$base.exe" else base
}

fun buildGoApplication(
    config: GoRunConfiguration,
    withCoverage: Boolean = false,
    handlerCallback: ((CapturingProcessHandler) -> Unit)? = null
): GoBuildResult {
    val sdkPath = GoProjectSettings.getInstance(config.project).state.goSdkPath
    val goExe = GoSdkUtils.getGoExecutablePath(sdkPath)
        ?: throw ExecutionException("Go SDK is not configured for this project.")

    val rawOutputPath = if (config.outputPath.isNotEmpty()) {
        FileUtil.toSystemDependentName(config.outputPath)
    } else {
        FileUtil.createTempFile("go_build_", if (SystemInfo.isWindows) ".exe" else "", true).absolutePath
    }

    // If output path is a directory (existing or trailing separator), resolve the binary inside it
    val outputDir = File(rawOutputPath)
    val outputExe = if (outputDir.isDirectory || rawOutputPath.endsWith(File.separator) || rawOutputPath.endsWith("/")) {
        outputDir.mkdirs()
        File(outputDir, deriveBinaryName(config)).absolutePath
    } else {
        rawOutputPath
    }

    val buildCmd = GeneralCommandLine(FileUtil.toSystemDependentName(goExe), "build", "-o", outputExe)

    if (withCoverage) {
        buildCmd.addParameter("-cover")
        buildCmd.addParameter("-coverpkg=./...")
        buildCmd.addParameter("-covermode=atomic")
    }

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

    // For coverage builds of a single file, build the whole package directory instead.
    // Passing a specific .go file creates an ad-hoc package where -coverpkg=./... doesn't
    // expand properly, resulting in empty coverage data.
    val buildTarget = if (withCoverage && config.runKind == GoRunKind.FILE) {
        val targetFile = File(FileUtil.toSystemDependentName(config.targetPath))
        FileUtil.toSystemDependentName(targetFile.parent ?: config.targetPath)
    } else {
        FileUtil.toSystemDependentName(config.targetPath)
    }
    buildCmd.addParameter(buildTarget)
    buildCmd.withWorkDirectory(FileUtil.toSystemDependentName(config.workingDirectory))

    val envVars = buildCmd.environment
    envVars.putAll(config.envVariables)
    if (config.goOS.isNotEmpty()) envVars[GoConstants.Env.GOOS] = config.goOS
    if (config.goArch.isNotEmpty()) envVars[GoConstants.Env.GOARCH] = config.goArch
    envVars[GoConstants.Env.CGO_ENABLED] = if (config.cgoEnabled) "1" else "0"
    if (config.goProxy.isNotEmpty()) envVars[GoConstants.Env.GOPROXY] = config.goProxy

    val sysDepSdkPath = FileUtil.toSystemDependentName(sdkPath)
    if (sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)) {
        envVars[GoConstants.Env.GOROOT] = sysDepSdkPath
        val oldPath = System.getenv("PATH") ?: ""
        val binPath = File(sysDepSdkPath, "bin").absolutePath
        envVars["PATH"] = "$binPath${File.pathSeparator}$oldPath"
    }

    val goPath = envVars[GoConstants.Env.GOPATH] ?: System.getenv(GoConstants.Env.GOPATH) ?: ""

    val handler = CapturingProcessHandler(buildCmd)
    handlerCallback?.invoke(handler)
    val result = handler.runProcess()

    return GoBuildResult(
        exitCode = result.exitCode,
        stdout = result.stdout,
        stderr = result.stderr,
        exePath = outputExe,
        buildCmdString = buildCmd.commandLineString,
        goRoot = sysDepSdkPath,
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
