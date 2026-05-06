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
import java.io.File

/*
 * Reconciles the Go versions specified in 'go.mod', 'go.work', and '.go-version' files 
 * with the currently selected project SDK. Provides notifications and automated actions 
 * to update these files when a version mismatch is detected.
 */
object GoModReconciler {
    private val LOG = Logger.getInstance(GoModReconciler::class.java)

    fun reconcile(project: Project, newSdkPath: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val basePath = project.basePath ?: return@executeOnPooledThread
            val newVersion = GoSdkVersion.retrieveGoVersion(newSdkPath) ?: return@executeOnPooledThread
            val rootDir = File(basePath)

            val goVersionFile = File(rootDir, GoConstants.FileExtensions.GO_VERSION)
            if (goVersionFile.exists()) {
                reconcileGoVersionFile(project, newVersion, goVersionFile)
            }

            val goWorkFile = File(rootDir, GoConstants.FileExtensions.WORK)
            if (goWorkFile.exists()) {
                reconcileWorkspace(project, newSdkPath, newVersion, goWorkFile)
                reconcileModules(project, newSdkPath, newVersion, rootDir, withinWorkspace = true)
            } else {
                reconcileModules(project, newSdkPath, newVersion, rootDir, withinWorkspace = false)
            }
        }
    }

    private fun reconcileGoVersionFile(project: Project, newVersion: String, goVersionFile: File) {
        val pinnedVersion = GoModDirectiveReader.readGoVersionFile(goVersionFile) ?: return
        if (pinnedVersion == newVersion) return

        ApplicationManager.getApplication().invokeLater {
            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("Go Plugin Notifications")
                .createNotification(
                    "Go Version Mismatch",
                    "${GoConstants.FileExtensions.GO_VERSION} pins Go $pinnedVersion but selected SDK is Go $newVersion.",
                    NotificationType.WARNING
                )
            notification.addAction(NotificationAction.createSimple("Update ${GoConstants.FileExtensions.GO_VERSION} to $newVersion") {
                notification.expire()
                try {
                    goVersionFile.writeText(newVersion)
                } catch (e: Exception) {
                    LOG.warn("Failed to update ${GoConstants.FileExtensions.GO_VERSION}: ${e.message}")
                }
            })
            notification.notify(project)
        }
    }

    private fun reconcileWorkspace(project: Project, newSdkPath: String, newVersion: String, goWorkFile: File) {
        val requiredVersion = GoModDirectiveReader.readGoDirective(goWorkFile) ?: return
        val comparison = GoSdkVersion.compareVersions(newVersion, requiredVersion)
        val workspaceDir = goWorkFile.parentFile

        ApplicationManager.getApplication().invokeLater {
            if (comparison < 0) {
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Go Plugin Notifications")
                    .createNotification(
                        "Go SDK Downgraded",
                        "Selected SDK (Go $newVersion) is older than go.work requirement (Go $requiredVersion).",
                        NotificationType.WARNING
                    )
                notification.addAction(NotificationAction.createSimple("Downgrade go.work to $newVersion") {
                    notification.expire()
                    runModCommand(project, newSdkPath, listOf("work", "edit", "-go=$newVersion", "-toolchain=go$newVersion"), workspaceDir)
                })
                notification.notify(project)
            } else if (comparison > 0) {
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("Go Plugin Notifications")
                    .createNotification(
                        "Go SDK Upgraded",
                        "Selected SDK (Go $newVersion) is newer than go.work requirement (Go $requiredVersion).",
                        NotificationType.INFORMATION
                    )
                notification.addAction(NotificationAction.createSimple("Upgrade go.work to $newVersion") {
                    notification.expire()
                    runModCommand(project, newSdkPath, listOf("work", "edit", "-go=$newVersion", "-toolchain=go$newVersion"), workspaceDir)
                })
                notification.notify(project)
            }
        }
    }

    private fun reconcileModules(project: Project, newSdkPath: String, newVersion: String, rootDir: File, withinWorkspace: Boolean = false) {
        val allGoMods = rootDir.walkTopDown()
            .maxDepth(6)
            .onEnter { it.name != "vendor" && it.name != ".git" }
            .filter { it.name == GoConstants.FileExtensions.MOD }
            .toList()

        for (goModFile in allGoMods) {
            val requiredVersion = GoModDirectiveReader.readGoDirective(goModFile) ?: continue
            val comparison = GoSdkVersion.compareVersions(newVersion, requiredVersion)
            val moduleDir = goModFile.parentFile
            val moduleLabel = moduleDir.relativeTo(rootDir).path.ifEmpty { "root" }

            val workspaceNote = if (withinWorkspace)
                " Note: go.work takes precedence inside the workspace, but this module's go.mod may matter when used outside it."
            else ""

            ApplicationManager.getApplication().invokeLater {
                if (comparison < 0) {
                    val notification = NotificationGroupManager.getInstance()
                        .getNotificationGroup("Go Plugin Notifications")
                        .createNotification(
                            "Go SDK Downgraded",
                            "Selected SDK (Go $newVersion) is older than go.mod requirement (Go $requiredVersion) in module '$moduleLabel'.$workspaceNote",
                            NotificationType.WARNING
                        )
                    notification.addAction(NotificationAction.createSimple("Downgrade go.mod to $newVersion") {
                        notification.expire()
                        runModCommand(project, newSdkPath, listOf("mod", "edit", "-go=$newVersion", "-toolchain=go$newVersion"), moduleDir)
                    })
                    notification.notify(project)
                } else if (comparison > 0) {
                    val notification = NotificationGroupManager.getInstance()
                        .getNotificationGroup("Go Plugin Notifications")
                        .createNotification(
                            "Go SDK Upgraded",
                            "Selected SDK (Go $newVersion) is newer than go.mod requirement (Go $requiredVersion) in module '$moduleLabel'.$workspaceNote",
                            NotificationType.INFORMATION
                        )
                    notification.addAction(NotificationAction.createSimple("Upgrade go.mod to $newVersion") {
                        notification.expire()
                        runModCommand(project, newSdkPath, listOf("get", "go@$newVersion", "toolchain@$newVersion"), moduleDir)
                    })
                    notification.notify(project)
                }
            }
        }
    }

    private fun runModCommand(project: Project, sdkPath: String, args: List<String>, workDir: File) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Updating go.mod...", true) {
            override fun run(indicator: ProgressIndicator) {
                val goExe = GoSdkLocator.getGoExecutablePath(sdkPath) ?: return
                try {
                    val cmd = GeneralCommandLine(listOf(goExe) + args)
                        .withWorkDirectory(workDir)
                        .withEnvironment(GoConstants.Env.GOROOT, sdkPath)
                    val handler = CapturingProcessHandler(cmd)
                    val output = handler.runProcess(30000)
                    if (output.exitCode != 0) {
                        LOG.warn("Failed to update go.mod: ${output.stderr}")
                    }
                } catch (e: Exception) {
                    LOG.error("Error running go command", e)
                }
            }
        })
    }
}
