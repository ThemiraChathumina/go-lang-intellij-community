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
import java.io.File

object GoModReconciler {
    private val LOG = Logger.getInstance(GoModReconciler::class.java)

    fun reconcile(project: Project, newSdkPath: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val basePath = project.basePath ?: return@executeOnPooledThread
            val newVersion = GoSdkUtils.retrieveGoVersion(newSdkPath) ?: return@executeOnPooledThread
            val rootDir = File(basePath)
            val goWorkFile = File(rootDir, "go.work")

            if (goWorkFile.exists()) {
                reconcileWorkspace(project, newSdkPath, newVersion, goWorkFile)
            } else {
                reconcileModules(project, newSdkPath, newVersion, rootDir)
            }
        }
    }

    private fun reconcileWorkspace(project: Project, newSdkPath: String, newVersion: String, goWorkFile: File) {
        val requiredVersion = GoSdkUtils.readGoDirective(goWorkFile) ?: return
        val comparison = compareVersions(newVersion, requiredVersion)
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

    private fun reconcileModules(project: Project, newSdkPath: String, newVersion: String, rootDir: File) {
        val allGoMods = rootDir.walkTopDown()
            .maxDepth(6)
            .onEnter { it.name != "vendor" && it.name != ".git" }
            .filter { it.name == "go.mod" }
            .toList()

        for (goModFile in allGoMods) {
            val requiredVersion = GoSdkUtils.readGoDirective(goModFile) ?: continue
            val comparison = compareVersions(newVersion, requiredVersion)
            val moduleDir = goModFile.parentFile
            val moduleLabel = moduleDir.relativeTo(rootDir).path.ifEmpty { "root" }

            ApplicationManager.getApplication().invokeLater {
                if (comparison < 0) {
                    val notification = NotificationGroupManager.getInstance()
                        .getNotificationGroup("Go Plugin Notifications")
                        .createNotification(
                            "Go SDK Downgraded",
                            "Selected SDK (Go $newVersion) is older than go.mod requirement (Go $requiredVersion) in module '$moduleLabel'.",
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
                            "Selected SDK (Go $newVersion) is newer than go.mod requirement (Go $requiredVersion) in module '$moduleLabel'.",
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

    private fun runModCommand(project: Project, sdkPath: String, args: List<String>, workDir: File) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Updating go.mod...", true) {
            override fun run(indicator: ProgressIndicator) {
                val goExe = GoSdkUtils.getGoExecutablePath(sdkPath) ?: return
                try {
                    val cmd = GeneralCommandLine(listOf(goExe) + args)
                        .withWorkDirectory(workDir)
                        .withEnvironment("GOROOT", sdkPath)
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
