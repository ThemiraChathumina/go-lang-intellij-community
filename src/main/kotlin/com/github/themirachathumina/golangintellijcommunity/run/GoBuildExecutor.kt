package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkLocator
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import java.io.File

/*
 * Builds a Go application using `go build`, returning a GoBuildResult with the exit code,
 * output, and path to the compiled binary. Handles coverage instrumentation, race detection,
 * build tags, gcflags, and Go environment variables.
 */
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

fun buildGoApplication(
    config: GoRunConfiguration,
    withCoverage: Boolean = false,
    handlerCallback: ((CapturingProcessHandler) -> Unit)? = null
): GoBuildResult {
    val sdkPath = GoProjectSettings.getInstance(config.project).state.goSdkPath
    val goExe = GoSdkLocator.getGoExecutablePath(sdkPath)
        ?: throw ExecutionException("Go SDK is not configured for this project.")

    val rawOutputPath = if (config.outputPath.isNotEmpty()) {
        FileUtil.toSystemDependentName(config.outputPath)
    } else {
        FileUtil.createTempFile("go_build_", if (SystemInfo.isWindows) ".exe" else "", true).absolutePath
    }

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
    if (sdkPath.isNotEmpty() && GoSdkLocator.isValidSdkHome(sdkPath)) {
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
