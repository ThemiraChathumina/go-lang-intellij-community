package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.util.FileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.ProcessRunner
import com.github.themirachathumina.golangintellijcommunity.util.RealFileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.RealProcessRunner
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
import java.io.File

class GoSdkAutoDetector(
    private val fs: FileSystemAccess = RealFileSystemAccess(),
    private val runner: ProcessRunner = RealProcessRunner()
) : ProjectActivity {
    private val LOG = Logger.getInstance(GoSdkAutoDetector::class.java)

    companion object {
        private val GOTOOLCHAIN_VERSION_REGEX = Regex("""^go(\d+\.\d+(?:\.\d+)?)""")
    }

    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return
        val isGoProject = fs.walk(basePath, maxDepth = 2).any {
            val name = fs.getName(it)
            name == GoConstants.FileExtensions.WORK || name == GoConstants.FileExtensions.MOD || name.endsWith(GoConstants.FileExtensions.GO)
        }
        if (!isGoProject) return

        val settings = GoProjectSettings.getInstance(project)
        if (settings.state.goSdkPath.isNotEmpty() && GoSdkLocator.isValidSdkHome(settings.state.goSdkPath)) {
            GoGlobalSdkRegistry.getInstance().refreshEnvCache(settings.state.goSdkPath)
            GoplsInstaller.checkAndStart(project, settings.state.goSdkPath)
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            resolveSdkForProject(project, basePath)
        }
    }

    private fun resolveSdkForProject(project: Project, rootDir: String) {
        val goVersionFile = fs.combine(rootDir, GoConstants.FileExtensions.GO_VERSION)
        val requiredVersion: String? = if (fs.exists(goVersionFile)) {
            LOG.info("${GoConstants.Log.PREFIX} Found .go-version at $goVersionFile, using its version")
            GoModDirectiveReader(fs).readGoVersionFile(goVersionFile)
        } else {
            val goWorkFile = fs.combine(rootDir, GoConstants.FileExtensions.WORK)
            if (fs.exists(goWorkFile)) {
                LOG.info("${GoConstants.Log.PREFIX} Found go.work at $goWorkFile, using its go directive")
                GoModDirectiveReader(fs).readGoDirective(goWorkFile)
            } else {
                val allGoMods = fs.walk(rootDir, maxDepth = 6)
                    .filter {
                        val name = fs.getName(it)
                        name == GoConstants.FileExtensions.MOD && !it.contains("/vendor/") && !it.contains("/.git/")
                    }
                    .toList()
                allGoMods
                    .mapNotNull { GoModDirectiveReader(fs).readGoDirective(it) }
                    .maxWithOrNull(Comparator { a, b -> GoSdkVersion.compareVersions(a, b) })
            }
        }

        val gotoolchain = GoEnvironmentReader(fs, runner).readGoEnvFile()[GoConstants.Env.GOTOOLCHAIN]
            ?: fs.getEnv(GoConstants.Env.GOTOOLCHAIN)
        val toolchainVersion = gotoolchain
            ?.let { GOTOOLCHAIN_VERSION_REGEX.find(it)?.groupValues?.get(1) }

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

        val systemPaths = GoSdkLocator(fs, runner).suggestSdkHomePaths()
        val systemGo = systemPaths.firstOrNull()

        if (systemGo != null) {
            val sysVersion = GoSdkVersion(fs, runner).retrieveGoVersion(systemGo)
            if (sysVersion != null && requiredVersion != null) {
                if (GoSdkVersion.compareVersions(sysVersion, requiredVersion) >= 0) {
                    registry.addSdkPath(systemGo)
                    assignAndStart(project, systemGo)
                    return
                } else if (GoSdkVersion.compareVersions(sysVersion, GoConstants.Sdk.MIN_VERSION) >= 0) {
                    val downloadedRoot = runGoEnvGoroot(systemGo, rootDir)
                    if (downloadedRoot != null && GoSdkLocator(fs, runner).isValidSdkHome(downloadedRoot)) {
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

    private fun runGoEnvGoroot(systemGoPath: String, workDir: String): String? {
        val goExe = GoSdkLocator(fs, runner).getGoExecutablePath(systemGoPath) ?: return null
        return try {
            val output = runner.run(listOf(goExe, "env", GoConstants.Env.GOROOT), workDir = workDir, timeoutMs = 30000)
            if (output.exitCode == 0) output.stdout.trim() else null
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
