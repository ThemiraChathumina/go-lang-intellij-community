package com.github.themirachathumina.golangintellijcommunity.sdk

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
import com.redhat.devtools.lsp4ij.LanguageServerManager
import java.io.File

class GoSdkAutoDetector : ProjectActivity {
    private val LOG = Logger.getInstance(GoSdkAutoDetector::class.java)

    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return
        val rootDir = File(basePath)
        val isGoProject = rootDir.walkTopDown().maxDepth(2).any { it.name == "go.work" || it.name == "go.mod" || it.name.endsWith(".go") }
        if (!isGoProject) return

        val settings = GoProjectSettings.getInstance(project)
        if (settings.state.goSdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(settings.state.goSdkPath)) {
            GoGlobalSdkRegistry.getInstance().refreshEnvCache(settings.state.goSdkPath)
            checkAndStartLanguageServer(project, settings.state.goSdkPath)
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            resolveSdkForProject(project, rootDir)
        }
    }

    private fun resolveSdkForProject(project: Project, rootDir: File) {
        // .go-version has highest precedence — used by goenv/asdf/mise to pin project toolchain
        val goVersionFile = File(rootDir, ".go-version")
        val requiredVersion: String? = if (goVersionFile.exists()) {
            LOG.info("[Go-Lang-Community] Found .go-version at ${goVersionFile.absolutePath}, using its version")
            GoSdkUtils.readGoVersionFile(goVersionFile)
        } else {
            // go.work is authoritative for the whole workspace if present
            val goWorkFile = File(rootDir, "go.work")
            if (goWorkFile.exists()) {
                LOG.info("[Go-Lang-Community] Found go.work at ${goWorkFile.absolutePath}, using its go directive")
                GoSdkUtils.readGoDirective(goWorkFile)
            } else {
                // No go.work — collect all go.mod files and pick the highest required version
                val allGoMods = rootDir.walkTopDown()
                    .maxDepth(6)
                    .onEnter { it.name != "vendor" && it.name != ".git" }
                    .filter { it.name == "go.mod" }
                    .toList()
                allGoMods
                    .mapNotNull { GoSdkUtils.readGoDirective(it) }
                    .maxWithOrNull(Comparator { a, b -> compareVersions(a, b) })
            }
        }

        // GOTOOLCHAIN from the Go env file pins or constrains the toolchain version.
        // Format: "local" | "auto" | "go1.22.0" | "go1.22.0+auto"
        // If pinned to a specific version, treat it as the preferred SDK version.
        val gotoolchain = GoSdkUtils.readGoEnvFile()["GOTOOLCHAIN"]
            ?: System.getenv("GOTOOLCHAIN")
        val toolchainVersion = gotoolchain
            ?.let { Regex("""^go(\d+\.\d+(?:\.\d+)?)""").find(it)?.groupValues?.get(1) }

        val registry = GoGlobalSdkRegistry.getInstance()
        val allSdks = registry.getSdkPaths()

        // If GOTOOLCHAIN pins a specific version and we have it, use it directly
        if (toolchainVersion != null) {
            val toolchainMatch = allSdks.find { GoSdkUtils.retrieveGoVersion(it) == toolchainVersion }
            if (toolchainMatch != null) {
                LOG.info("[Go-Lang-Community] Using SDK pinned by GOTOOLCHAIN=$gotoolchain: $toolchainMatch")
                assignAndStart(project, toolchainMatch)
                return
            }
        }

        if (requiredVersion != null) {
            val exactMatch = allSdks.find { GoSdkUtils.retrieveGoVersion(it) == requiredVersion }
            if (exactMatch != null) {
                assignAndStart(project, exactMatch)
                return
            }
        }

        val bestFallback = allSdks.maxByOrNull { GoSdkUtils.retrieveGoVersion(it) ?: "" }
        if (bestFallback != null && requiredVersion == null) {
            assignAndStart(project, bestFallback)
            return
        }

        val systemPaths = GoSdkUtils.suggestSdkHomePaths()
        val systemGo = systemPaths.firstOrNull()

        if (systemGo != null) {
            val sysVersion = GoSdkUtils.retrieveGoVersion(systemGo)
            if (sysVersion != null && requiredVersion != null) {
                if (compareVersions(sysVersion, requiredVersion) >= 0) {
                    registry.addSdkPath(systemGo)
                    assignAndStart(project, systemGo)
                    return
                } else if (compareVersions(sysVersion, "1.21") >= 0) {
                    val downloadedRoot = runGoEnvGoroot(systemGo, rootDir)
                    if (downloadedRoot != null && GoSdkUtils.isValidSdkHome(downloadedRoot)) {
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

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }

    private fun runGoEnvGoroot(systemGoPath: String, workDir: File): String? {
        val goExe = GoSdkUtils.getGoExecutablePath(systemGoPath) ?: return null
        return try {
            val cmd = GeneralCommandLine(FileUtil.toSystemDependentName(goExe), "env", "GOROOT").withWorkDirectory(workDir)
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
            checkAndStartLanguageServer(project, path)
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
                            val targetVer = "go$requiredVersion"
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

    private fun checkAndStartLanguageServer(project: Project, sdkHome: String) {
        if (GoSdkUtils.isGoplsInstalled(sdkHome)) {
            ApplicationManager.getApplication().invokeLater {
                LOG.info("GoSdkAutoDetector: Starting gopls for ${project.name}")
                LanguageServerManager.getInstance(project).start("gopls")
            }
        } else {
            promptGoplsInstallation(project, sdkHome)
        }
    }

    private fun promptGoplsInstallation(project: Project, sdkHome: String) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Plugin Notifications")
            .createNotification(
                "Missing Go Language Server",
                "The 'gopls' executable is required for code completion, formatting, and diagnostics.",
                NotificationType.WARNING
            )

        notification.addAction(NotificationAction.createSimple("Install gopls") {
            notification.expire()
            installGoplsInBackground(project, sdkHome)
        })

        notification.notify(project)
    }

    private fun installGoplsInBackground(project: Project, sdkHome: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Installing gopls...", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Downloading and compiling golang.org/x/tools/gopls@latest..."

                val goExe = GoSdkUtils.getGoExecutablePath(sdkHome)
                if (goExe == null) {
                    LOG.error("Cannot install gopls: Go executable not found.")
                    return
                }

                try {
                    val cmd = GeneralCommandLine(FileUtil.toSystemDependentName(goExe), "install", "golang.org/x/tools/gopls@latest")
                        .withEnvironment("GOROOT", FileUtil.toSystemDependentName(sdkHome))
                    val handler = CapturingProcessHandler(cmd)
                    val output = handler.runProcess(120 * 1000)

                    if (output.exitCode == 0) {
                        LOG.info("Successfully installed gopls.")
                        ApplicationManager.getApplication().invokeLater {
                            LanguageServerManager.getInstance(project).start("gopls")
                        }
                    } else {
                        LOG.warn("Failed to install gopls: ${output.stderr}")
                    }
                } catch (e: Exception) {
                    LOG.error("Error during gopls installation", e)
                }
            }
        })
    }
}
