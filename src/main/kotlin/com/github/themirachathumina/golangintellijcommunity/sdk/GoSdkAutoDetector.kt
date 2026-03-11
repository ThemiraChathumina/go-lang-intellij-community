package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.io.FileUtil
import com.redhat.devtools.lsp4ij.LanguageServerManager
import java.io.File

class GoSdkAutoDetector : ProjectActivity {
    private val LOG = Logger.getInstance(GoSdkAutoDetector::class.java)

    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return

        val rootDir = File(basePath)
        val isGoProject = rootDir.walkTopDown().maxDepth(2).any {
            it.name == "go.mod" || it.name.endsWith(".go")
        }

        if (!isGoProject) return

        // 2. Check the currently assigned SDK
        val currentSdk = ProjectRootManager.getInstance(project).projectSdk
        if (currentSdk != null && currentSdk.sdkType is GoSdkType) {
            val homePath = currentSdk.homePath

            if (homePath != null && GoSdkUtils.isValidSdkHome(homePath)) {
                LOG.info("GoSdkAutoDetector: Valid Go SDK already assigned: ${currentSdk.name}")
                // THE FIX: Check for gopls before starting!
                checkAndStartLanguageServer(project, currentSdk)
                return
            } else {
                LOG.warn("GoSdkAutoDetector: Assigned Go SDK '${currentSdk.name}' is invalid.")
            }
        }

        // 3. Search for a valid Go SDK on the system
        val suggestedPaths = GoSdkUtils.suggestSdkHomePaths()
        if (suggestedPaths.isEmpty()) return

        val bestPath = suggestedPaths.first()
        GoSdkUtils.retrieveGoVersion(bestPath)

        val goSdkType = GoSdkType.getInstance()

        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                val existingSdk = ProjectJdkTable.getInstance().allJdks.find {
                    it.sdkType == goSdkType &&
                            FileUtil.toSystemIndependentName(it.homePath ?: "") == FileUtil.toSystemIndependentName(bestPath)
                }

                val sdk = existingSdk ?: SdkConfigurationUtil.createAndAddSDK(bestPath, goSdkType)
                if (sdk != null) {
                    ProjectRootManager.getInstance(project).projectSdk = sdk
                    SdkConfigurationUtil.setDirectoryProjectSdk(project, sdk)

                    val modules = ModuleManager.getInstance(project).modules
                    for (module in modules) {
                        ModuleRootModificationUtil.setSdkInherited(module)
                    }

                    // THE FIX: Check for gopls before starting!
                    checkAndStartLanguageServer(project, sdk)
                }
            }
        }
    }

    // --- NEW LOGIC: Manage the gopls lifecycle ---

    private fun checkAndStartLanguageServer(project: Project, sdk: Sdk) {
        val sdkHome = sdk.homePath ?: return

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
            notification.expire() // Dismiss the balloon when clicked
            installGoplsInBackground(project, sdkHome)
        })

        notification.notify(project)
    }

    private fun installGoplsInBackground(project: Project, sdkHome: String) {
        // This spins up the native IntelliJ background progress bar in the bottom right corner
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
                    val cmd = GeneralCommandLine(goExe, "install", "golang.org/x/tools/gopls@latest")
                    val handler = CapturingProcessHandler(cmd)

                    // Allow up to 2 minutes. Compiling gopls downloads a lot of dependencies!
                    val output = handler.runProcess(120 * 1000)

                    if (output.exitCode == 0) {
                        LOG.info("Successfully installed gopls.")

                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Go Plugin Notifications")
                            .createNotification(
                                "Go Language Server Installed",
                                "gopls was successfully installed and started.",
                                NotificationType.INFORMATION
                            )
                            .notify(project)

                        // It's installed! Tell lsp4ij to start it up.
                        ApplicationManager.getApplication().invokeLater {
                            LanguageServerManager.getInstance(project).start("gopls")
                        }
                    } else {
                        LOG.warn("Failed to install gopls: ${output.stderr}")
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Go Plugin Notifications")
                            .createNotification("Installation Failed", "Could not install gopls. Check idea.log for details.", NotificationType.ERROR)
                            .notify(project)
                    }
                } catch (e: Exception) {
                    LOG.error("Error during gopls installation", e)
                }
            }
        })
    }
}