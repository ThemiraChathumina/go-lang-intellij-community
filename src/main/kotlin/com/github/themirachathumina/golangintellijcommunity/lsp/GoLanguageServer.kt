package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.github.themirachathumina.golangintellijcommunity.settings.GoAppSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider
import com.intellij.openapi.vfs.VirtualFile
import org.eclipse.lsp4j.jsonrpc.messages.Message
import org.eclipse.lsp4j.services.LanguageServer
import org.jetbrains.annotations.NotNull
import java.io.File

class GoLanguageServer(@NotNull project: Project) : OSProcessStreamConnectionProvider() {
    private val LOG = Logger.getInstance(GoLanguageServer::class.java)
    private val semanticLogger = GoLspSemanticLogger()

    init {
        LOG.info("${GoConstants.Log.PREFIX} Initializing GoLanguageServer for project: ${project.name}")
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath

        val goplsExe = GoSdkUtils.getGoplsExecutablePath(if (sdkPath.isNotEmpty()) sdkPath else null) ?: GoSdkUtils.GOPLS_EXECUTABLE_NAME
        LOG.info("${GoConstants.Log.PREFIX} Using gopls executable: $goplsExe")

        val commandLine = GeneralCommandLine(FileUtil.toSystemDependentName(goplsExe), "serve")

        if (sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)) {
            val sysDepSdkPath = FileUtil.toSystemDependentName(sdkPath)
            commandLine.withEnvironment(GoConstants.Env.GOROOT, sysDepSdkPath)
            val oldPath = System.getenv("PATH") ?: ""
            val binPath = File(sysDepSdkPath, "bin").absolutePath
            commandLine.withEnvironment("PATH", "$binPath${File.pathSeparator}$oldPath")
        }

        val effectiveGoPath = GoSdkUtils.resolveGoPath().map { FileUtil.toSystemDependentName(it) }.joinToString(File.pathSeparator)
        val systemGoPath = System.getenv(GoConstants.Env.GOPATH) ?: ""
        if (effectiveGoPath != systemGoPath) {
            commandLine.withEnvironment(GoConstants.Env.GOPATH, effectiveGoPath)
            LOG.info("${GoConstants.Log.PREFIX} Setting GOPATH for gopls: $effectiveGoPath")
        }

        val effectiveGoModCache = FileUtil.toSystemDependentName(GoSdkUtils.resolveGoModCache())
        val systemGoModCache = System.getenv(GoConstants.Env.GOMODCACHE) ?: ""
        if (effectiveGoModCache != systemGoModCache) {
            commandLine.withEnvironment(GoConstants.Env.GOMODCACHE, effectiveGoModCache)
            LOG.info("${GoConstants.Log.PREFIX} Setting GOMODCACHE for gopls: $effectiveGoModCache")
        }

        super.setCommandLine(commandLine)
    }

    override fun getInitializationOptions(rootUri: VirtualFile?): Any? {
        if (!GoAppSettings.getInstance().state.semanticTokensEnabled) return null
        return mapOf("semanticTokens" to true)
    }

    // handleMessage is called for ALL messages — both client→server and server→client.
    override fun handleMessage(message: Message, languageServer: LanguageServer?, rootUri: VirtualFile?) {
        if (GoAppSettings.getInstance().state.logSemanticTokens) {
            semanticLogger.onMessage(message)
        }
    }
}
