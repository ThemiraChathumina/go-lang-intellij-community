package com.github.themirachathumina.golangintellijcommunity.lsp

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.sdk.GoEnvironmentReader
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkLocator
import com.github.themirachathumina.golangintellijcommunity.settings.GoAppSettings
import com.github.themirachathumina.golangintellijcommunity.util.FileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.RealFileSystemAccess
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

/*
 * Configures and starts the gopls language server process for a project. Sets GOROOT, PATH,
 * GOPATH, and GOMODCACHE environment variables before launch, and optionally enables
 * semantic tokens via initialization options.
 */
class GoLanguageServer(@NotNull project: Project, private val fs: FileSystemAccess = RealFileSystemAccess()) : OSProcessStreamConnectionProvider() {
    private val LOG = Logger.getInstance(GoLanguageServer::class.java)
    private val semanticLogger = GoLspSemanticLogger()

    init {
        LOG.info("${GoConstants.Log.PREFIX} Initializing GoLanguageServer for project: ${project.name}")
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath

        val goplsExe = GoSdkLocator(fs).getGoplsExecutablePath(if (sdkPath.isNotEmpty()) sdkPath else null) ?: GoSdkLocator.GOPLS_EXECUTABLE_NAME
        LOG.info("${GoConstants.Log.PREFIX} Using gopls executable: $goplsExe")

        val commandLine = GeneralCommandLine(goplsExe, "serve")

        if (sdkPath.isNotEmpty() && GoSdkLocator(fs).isValidSdkHome(sdkPath)) {
            commandLine.withEnvironment(GoConstants.Env.GOROOT, sdkPath)
            val oldPath = fs.getEnv("PATH") ?: ""
            val binPath = fs.combine(sdkPath, "bin")
            commandLine.withEnvironment("PATH", "$binPath${File.pathSeparator}$oldPath")
        }

        val effectiveGoPath = GoEnvironmentReader(fs).resolveGoPath().joinToString(File.pathSeparator)
        val systemGoPath = fs.getEnv(GoConstants.Env.GOPATH) ?: ""
        if (effectiveGoPath != systemGoPath) {
            commandLine.withEnvironment(GoConstants.Env.GOPATH, effectiveGoPath)
            LOG.info("${GoConstants.Log.PREFIX} Setting GOPATH for gopls: $effectiveGoPath")
        }

        val effectiveGoModCache = GoEnvironmentReader(fs).resolveGoModCache()
        val systemGoModCache = fs.getEnv(GoConstants.Env.GOMODCACHE) ?: ""
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

    override fun handleMessage(message: Message, languageServer: LanguageServer?, rootUri: VirtualFile?) {
        if (GoAppSettings.getInstance().state.logSemanticTokens) {
            semanticLogger.onMessage(message)
        }
    }
}
