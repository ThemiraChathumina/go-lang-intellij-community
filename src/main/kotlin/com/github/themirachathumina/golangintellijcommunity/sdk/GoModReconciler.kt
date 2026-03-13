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
            val goModFile = File(basePath, "go.mod")
            if (!goModFile.exists()) return@executeOnPooledThread

            val content = goModFile.readText()
            val toolchainMatch = Regex("""^toolchain\s+go([\d.]+)""", RegexOption.MULTILINE).find(content)
            val goMatch = Regex("""^go\s+([\d.]+)""", RegexOption.MULTILINE).find(content)
            val requiredVersion = toolchainMatch?.groupValues?.get(1) ?: goMatch?.groupValues?.get(1) ?: return@executeOnPooledThread

            val newVersion = GoSdkUtils.retrieveGoVersion(newSdkPath) ?: return@executeOnPooledThread

            val comparison = compareVersions(newVersion, requiredVersion)
            
            ApplicationManager.getApplication().invokeLater {
                if (comparison < 0) {
                    val notification = NotificationGroupManager.getInstance()
                        .getNotificationGroup("Go Plugin Notifications")
                        .createNotification(
                            "Go SDK Downgraded",
                            "Selected SDK (Go $newVersion) is older than go.mod requirement (Go $requiredVersion).",
                            NotificationType.WARNING
                        )
                    notification.addAction(NotificationAction.createSimple("Downgrade go.mod to $newVersion") {
                        notification.expire()
                        runModCommand(project, newSdkPath, listOf("mod", "edit", "-go=$newVersion", "-toolchain=go$newVersion"))
                    })
                    notification.notify(project)
                } else if (comparison > 0) {
                    val notification = NotificationGroupManager.getInstance()
                        .getNotificationGroup("Go Plugin Notifications")
                        .createNotification(
                            "Go SDK Upgraded",
                            "Selected SDK (Go $newVersion) is newer than go.mod requirement (Go $requiredVersion).",
                            NotificationType.INFORMATION
                        )
                    notification.addAction(NotificationAction.createSimple("Upgrade go.mod to $newVersion") {
                        notification.expire()
                        runModCommand(project, newSdkPath, listOf("get", "go@$newVersion", "toolchain@$newVersion"))
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

    private fun runModCommand(project: Project, sdkPath: String, args: List<String>) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Updating go.mod...", true) {
            override fun run(indicator: ProgressIndicator) {
                val goExe = GoSdkUtils.getGoExecutablePath(sdkPath) ?: return
                try {
                    val cmd = GeneralCommandLine(listOf(goExe) + args)
                        .withWorkDirectory(project.basePath)
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
