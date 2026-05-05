package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.io.FileUtil
import java.io.File

class GoSdkAutoDetector : ProjectActivity {
    private val LOG = Logger.getInstance(GoSdkAutoDetector::class.java)

    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return
        val rootDir = File(basePath)
        val isGoProject = rootDir.walkTopDown().maxDepth(2).any {
            it.name == GoConstants.FileExtensions.WORK || it.name == GoConstants.FileExtensions.MOD || it.name.endsWith(GoConstants.FileExtensions.GO)
        }
        if (!isGoProject) return

        val settings = GoProjectSettings.getInstance(project)
        if (settings.state.goSdkPath.isNotEmpty() && GoSdkLocator.isValidSdkHome(settings.state.goSdkPath)) {
            GoGlobalSdkRegistry.getInstance().refreshEnvCache(settings.state.goSdkPath)
            GoplsInstaller.checkAndStart(project, settings.state.goSdkPath)
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            resolveSdkForProject(project, rootDir)
        }
    }

    private fun resolveSdkForProject(project: Project, rootDir: File) {
        val goVersionFile = File(rootDir, GoConstants.FileExtensions.GO_VERSION)
        val requiredVersion: String? = if (goVersionFile.exists()) {
            LOG.info("${GoConstants.Log.PREFIX} Found .go-version at ${goVersionFile.absolutePath}, using its version")
            GoModDirectiveReader.readGoVersionFile(goVersionFile)
        } else {
            val goWorkFile = File(rootDir, GoConstants.FileExtensions.WORK)
            if (goWorkFile.exists()) {
                LOG.info("${GoConstants.Log.PREFIX} Found go.work at ${goWorkFile.absolutePath}, using its go directive")
                GoModDirectiveReader.readGoDirective(goWorkFile)
            } else {
                val allGoMods = rootDir.walkTopDown()
                    .maxDepth(6)
                    .onEnter { it.name != "vendor" && it.name != ".git" }
                    .filter { it.name == GoConstants.FileExtensions.MOD }
                    .toList()
                allGoMods
                    .mapNotNull { GoModDirectiveReader.readGoDirective(it) }
                    .maxWithOrNull(Comparator { a, b -> GoSdkVersion.compareVersions(a, b) })
            }
        }

        val gotoolchain = GoEnvironmentReader.readGoEnvFile()[GoConstants.Env.GOTOOLCHAIN]
            ?: System.getenv(GoConstants.Env.GOTOOLCHAIN)
        val toolchainVersion = gotoolchain
            ?.let { Regex("""^go(\d+\.\d+(?:\.\d+)?)""").find(it)?.groupValues?.get(1) }

        val registry = GoGlobalSdkRegistry.getInstance()
        val allSdks = registry.getSdkPaths()

        if (toolchainVersion != null) {
            val toolchainMatch = allSdks.find { GoSdkVersion.retrieveGoVersion(it) == toolchainVersion }
            if (toolchainMatch != null) {
                LOG.info("${GoConstants.Log.PREFIX} Using SDK pinned by GOTOOLCHAIN=$gotoolchain: $toolchainMatch")
                assignAndStart(project, toolchainMatch)
                return
            }
        }

        if (requiredVersion != null) {
            val exactMatch = allSdks.find { GoSdkVersion.retrieveGoVersion(it) == requiredVersion }
            if (exactMatch != null) {
                assignAndStart(project, exactMatch)
                return
            }
        }

        val bestFallback = allSdks.maxByOrNull { GoSdkVersion.retrieveGoVersion(it) ?: "" }
        if (bestFallback != null && requiredVersion == null) {
            assignAndStart(project, bestFallback)
            return
        }

        val systemPaths = GoSdkLocator.suggestSdkHomePaths()
        val systemGo = systemPaths.firstOrNull()

        if (systemGo != null) {
            val sysVersion = GoSdkVersion.retrieveGoVersion(systemGo)
            if (sysVersion != null && requiredVersion != null) {
                if (GoSdkVersion.compareVersions(sysVersion, requiredVersion) >= 0) {
                    registry.addSdkPath(systemGo)
                    assignAndStart(project, systemGo)
                    return
                } else if (GoSdkVersion.compareVersions(sysVersion, GoConstants.Sdk.MIN_VERSION) >= 0) {
                    val downloadedRoot = runGoEnvGoroot(systemGo, rootDir)
                    if (downloadedRoot != null && GoSdkLocator.isValidSdkHome(downloadedRoot)) {
                        registry.addSdkPath(downloadedRoot)
                        assignAndStart(project, downloadedRoot)
                        return
                    }
                }
            } else {
                registry.addSdkPath(systemGo)
                assignAndStart(project, systemGo)
                return
            }
        }

        promptMissingSdk(project, requiredVersion)
    }

    private fun runGoEnvGoroot(systemGoPath: String, workDir: File): String? {
        val goExe = GoSdkLocator.getGoExecutablePath(systemGoPath) ?: return null
        return try {
            val cmd = GeneralCommandLine(FileUtil.toSystemDependentName(goExe), "env", GoConstants.Env.GOROOT).withWorkDirectory(workDir)
            val handler = CapturingProcessHandler(cmd)
            val output = handler.runProcess(30000)
            if (output.exitCode == 0) FileUtil.toSystemIndependentName(output.stdout.trim()) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun assignAndStart(project: Project, path: String) {
        ApplicationManager.getApplication().invokeLater {
            GoProjectSettings.getInstance(project).setSdkPath(path)
            GoplsInstaller.checkAndStart(project, path)
        }
    }

    private fun promptMissingSdk(project: Project, requiredVersion: String?) {
        ApplicationManager.getApplication().invokeLater {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("Go Plugin Notifications")
                .createNotification(
                    "Missing Go SDK",
                    "This project requires Go ${requiredVersion ?: "SDK"}.",
                    NotificationType.WARNING
                )

            notification.addAction(NotificationAction.createSimple("Download Go ${requiredVersion ?: "SDK"}") {
                notification.expire()
                if (requiredVersion != null) {
                    ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Downloading Go SDK...", true) {
                        override fun run(indicator: ProgressIndicator) {
                            val targetVer = "${GoConstants.Sdk.VERSION_PREFIX}$requiredVersion"
                            val path = GoSdkDownloader.downloadSdk(targetVer, indicator)
                            if (path != null) {
                                assignAndStart(project, path)
                            }
                        }
                    })
                }
            })

            notification.notify(project)
        }
    }
}
