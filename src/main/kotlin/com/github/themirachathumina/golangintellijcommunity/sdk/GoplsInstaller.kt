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
import com.intellij.openapi.util.io.FileUtil
import com.redhat.devtools.lsp4ij.LanguageServerManager

object GoplsInstaller {
    private val LOG = Logger.getInstance(GoplsInstaller::class.java)

    fun checkAndStart(project: Project, sdkHome: String) {
        if (GoSdkLocator.isGoplsInstalled(sdkHome)) {
            ApplicationManager.getApplication().invokeLater {
                LOG.info("GoplsInstaller: Starting gopls for ${project.name}")
                LanguageServerManager.getInstance(project).start(GoConstants.Lsp.SERVER_ID)
            }
        } else {
            promptInstallation(project, sdkHome)
        }
    }

    private fun promptInstallation(project: Project, sdkHome: String) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Plugin Notifications")
            .createNotification(
                "Missing Go Language Server",
                "The 'gopls' executable is required for code completion, formatting, and diagnostics.",
                NotificationType.WARNING
            )

        notification.addAction(NotificationAction.createSimple("Install gopls") {
            notification.expire()
            installInBackground(project, sdkHome)
        })

        notification.notify(project)
    }

    private fun installInBackground(project: Project, sdkHome: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Installing gopls...", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Downloading and compiling golang.org/x/tools/gopls@latest..."

                val goExe = GoSdkLocator.getGoExecutablePath(sdkHome)
                if (goExe == null) {
                    LOG.error("Cannot install gopls: Go executable not found.")
                    return
                }

                try {
                    val cmd = GeneralCommandLine(FileUtil.toSystemDependentName(goExe), "install", "golang.org/x/tools/gopls@latest")
                        .withEnvironment(GoConstants.Env.GOROOT, FileUtil.toSystemDependentName(sdkHome))
                    val handler = CapturingProcessHandler(cmd)
                    val output = handler.runProcess(120 * 1000)

                    if (output.exitCode == 0) {
                        LOG.info("Successfully installed gopls.")
                        ApplicationManager.getApplication().invokeLater {
                            LanguageServerManager.getInstance(project).start(GoConstants.Lsp.SERVER_ID)
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
