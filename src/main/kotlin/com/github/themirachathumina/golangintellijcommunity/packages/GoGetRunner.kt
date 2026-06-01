package com.github.themirachathumina.golangintellijcommunity.packages

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

/*
 * Runs `go get` commands in a background task to install, update, or remove modules.
 * After each operation the go.mod and go.sum files are refreshed in the VFS so the
 * IDE picks up the changes immediately. onComplete is always called on the EDT.
 */
object GoGetRunner {

    fun install(
        project: Project, goExe: String, workDir: String,
        modulePath: String, version: String = "latest",
        onComplete: (success: Boolean) -> Unit = {}
    ) {
        val target = "$modulePath@$version"
        run(project, "Installing $modulePath", listOf(goExe, "get", target), workDir, onComplete)
    }

    fun remove(
        project: Project, goExe: String, workDir: String,
        modulePath: String,
        onComplete: (success: Boolean) -> Unit = {}
    ) {
        run(project, "Removing $modulePath", listOf(goExe, "get", "$modulePath@none"), workDir, onComplete)
    }

    fun updateAll(
        project: Project, goExe: String, workDir: String,
        onComplete: (success: Boolean) -> Unit = {}
    ) {
        run(project, "Updating all dependencies", listOf(goExe, "get", "-u", "./..."), workDir, onComplete)
    }

    private fun run(
        project: Project, title: String, command: List<String>, workDir: String,
        onComplete: (Boolean) -> Unit
    ) {
        object : Task.Backgroundable(project, title, false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true

                val (exitCode, output) = try {
                    val proc = ProcessBuilder(command)
                        .directory(File(workDir))
                        .redirectErrorStream(true)
                        .start()
                    val out = proc.inputStream.bufferedReader().readText().trim()
                    proc.waitFor() to out
                } catch (ex: Exception) {
                    -1 to (ex.message ?: "unknown error")
                }

                ApplicationManager.getApplication().invokeLater {
                    if (exitCode == 0) {
                        refreshModFiles(workDir)
                    } else if (output.isNotBlank()) {
                        notify(project, output, NotificationType.ERROR)
                    }
                    onComplete(exitCode == 0)
                }
            }
        }.queue()
    }

    private fun refreshModFiles(workDir: String) {
        LocalFileSystem.getInstance().refreshAndFindFileByPath("$workDir/go.mod")
        LocalFileSystem.getInstance().refreshAndFindFileByPath("$workDir/go.sum")
    }

    private fun notify(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Plugin Notifications")
            .createNotification(message, type)
            .notify(project)
    }
}
