package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkLocator
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * "Tools > Go > Clean Test Cache" — runs `go clean -testcache` in a background
 * task so the IDE stays responsive.  Notifies the user on completion or failure.
 */
class GoCleanTestCacheAction(
    private val resolveGoExe: (sdkPath: String) -> String? = GoSdkLocator::getGoExecutablePath,
    private val processLauncher: (command: List<String>) -> Pair<Int, String> = { cmd ->
        val proc = ProcessBuilder(cmd).redirectErrorStream(true).start()
        val out = proc.inputStream.bufferedReader().readText().trim()
        proc.waitFor() to out
    }
) : AnAction("Clean Go Test Cache") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        object : Task.Backgroundable(project, "Cleaning Go Test Cache", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running go clean -testcache…"
                indicator.isIndeterminate = true
                cleanTestCache(project)
            }
        }.queue()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }

    internal fun cleanTestCache(project: Project) {
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        val goExe = resolveGoExe(sdkPath)
        if (goExe == null) {
            notify(project, "Go SDK not configured — cannot clean test cache.", NotificationType.WARNING)
            return
        }

        val (exitCode, output) = processLauncher(listOf(goExe, "clean", "-testcache"))

        if (exitCode == 0) {
            notify(project, "Go test cache cleaned successfully.", NotificationType.INFORMATION)
        } else {
            val detail = if (output.isNotBlank()) "\n$output" else ""
            notify(project, "go clean -testcache failed (exit $exitCode).$detail", NotificationType.ERROR)
        }
    }

    private fun notify(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Plugin Notifications")
            .createNotification(message, type)
            .notify(project)
    }
}
