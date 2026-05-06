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
import com.redhat.devtools.lsp4ij.LanguageServerManager

/*
 * Manages the installation and startup of the gopls language server. 
 * Checks if gopls is installed and prompts the user to install it if it is missing, 
 * then starts the language server for the project.
 */
class GoplsInstaller(
    private val fs: FileSystemAccess = RealFileSystemAccess(),
    private val runner: ProcessRunner = RealProcessRunner()
) {
    companion object {
        private val LOG = Logger.getInstance(GoplsInstaller::class.java)
        private val DEFAULT = GoplsInstaller()

        fun checkAndStart(project: Project, sdkHome: String) = DEFAULT.checkAndStart(project, sdkHome)
    }

    fun checkAndStart(project: Project, sdkHome: String) {
        if (GoSdkLocator(fs, runner).isGoplsInstalled(sdkHome)) {
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

                val goExe = GoSdkLocator(fs, runner).getGoExecutablePath(sdkHome)
                if (goExe == null) {
                    LOG.error("Cannot install gopls: Go executable not found.")
                    return
                }

                try {
                    val output = runner.run(
                        listOf(goExe, "install", "golang.org/x/tools/gopls@latest"),
                        env = mapOf(GoConstants.Env.GOROOT to sdkHome),
                        timeoutMs = 120 * 1000
                    )

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
