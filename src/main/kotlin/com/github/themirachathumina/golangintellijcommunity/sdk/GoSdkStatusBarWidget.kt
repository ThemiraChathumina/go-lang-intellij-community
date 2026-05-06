package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.ui.ClickListener
import com.intellij.ui.components.JBLabel
import com.redhat.devtools.lsp4ij.LanguageServerManager
import java.awt.event.MouseEvent
import javax.swing.JComponent

/*
 * Status bar widget that displays the currently selected Go SDK version for the project.
 * Provides a popup menu for switching between SDKs, adding local SDKs, or downloading new versions.
 */
class GoSdkStatusBarWidget(private val project: Project) : CustomStatusBarWidget {
    private val LOG = Logger.getInstance(GoSdkStatusBarWidget::class.java)
    private val label = JBLabel()

    init {
        updateLabel()
        object : ClickListener() {
            override fun onClick(event: MouseEvent, clickCount: Int): Boolean {
                showPopup(event)
                return true
            }
        }.installOn(label)

        project.messageBus.connect(this).subscribe(GoProjectSettings.TOPIC, object : GoProjectSettingsListener {
            override fun sdkChanged(newPath: String) {
                ApplicationManager.getApplication().invokeLater({ updateLabel() }, ModalityState.any())
            }
        })

        ApplicationManager.getApplication().messageBus.connect(this).subscribe(GoGlobalSdkRegistry.TOPIC, object : GoGlobalSdkRegistryListener {
            override fun sdkRegistryChanged() {
                ApplicationManager.getApplication().invokeLater({ updateLabel() }, ModalityState.any())
            }
        })
    }

    private fun updateLabel() {
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        if (sdkPath.isNotEmpty() && GoSdkLocator.isValidSdkHome(sdkPath)) {
            val version = GoSdkVersion.retrieveGoVersion(sdkPath)
            label.icon = GoIcons.FILE
            label.text = if (version != null) "Go $version" else "Go SDK"
            label.toolTipText = "Current Go SDK: $sdkPath"
        } else {
            label.icon = GoIcons.FILE
            label.text = "No Go SDK"
            label.toolTipText = "Click to configure Go SDK"
        }
    }

    private fun showPopup(event: MouseEvent) {
        val registry = GoGlobalSdkRegistry.getInstance()
        val paths = registry.getSdkPaths()

        val versionMap = mutableMapOf<String, String>()
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            for (path in paths) {
                val version = GoSdkVersion.retrieveGoVersion(path)
                if (version != null) versionMap[path] = version
            }
        }, "Loading Go SDK Versions", false, project)

        val sortedPaths = paths.sortedByDescending { versionMap[it] ?: "" }

        val group = DefaultActionGroup()

        for (path in sortedPaths) {
            val version = versionMap[path] ?: "Unknown"
            group.add(object : AnAction("Go $version ($path)") {
                override fun actionPerformed(e: AnActionEvent) { setSdk(path) }
                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
            })
        }

        group.addSeparator()
        group.add(AddLocalSdkAction())
        group.add(DownloadSdkAction())

        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            "Select Go SDK",
            group,
            com.intellij.ide.DataManager.getInstance().getDataContext(label),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            false
        )
        popup.showInScreenCoordinates(label, event.locationOnScreen)
    }

    private fun setSdk(path: String) {
        GoProjectSettings.getInstance(project).setSdkPath(path)
        GoModReconciler.reconcile(project, path)
        ApplicationManager.getApplication().invokeLater {
            LanguageServerManager.getInstance(project).stop(GoConstants.Lsp.SERVER_ID)
            LanguageServerManager.getInstance(project).start(GoConstants.Lsp.SERVER_ID)
        }
    }

    private inner class AddLocalSdkAction : AnAction("Add Local SDK...") {
        override fun actionPerformed(e: AnActionEvent) {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            descriptor.title = "Select Go SDK Directory"
            FileChooser.chooseFile(descriptor, project, null) { file ->
                val path = file.path
                if (!GoSdkLocator.isValidSdkHome(path)) {
                    Messages.showErrorDialog(project, "The selected directory is not a valid Go SDK.", "Invalid SDK")
                    return@chooseFile
                }
                val version = GoSdkVersion.retrieveGoVersion(path)
                if (version != null && !GoSdkVersion.isSupportedVersion(version)) {
                    Messages.showErrorDialog(project, "Go $version is not supported. Minimum required version is Go ${GoConstants.Sdk.MIN_VERSION}.", "Unsupported SDK Version")
                    return@chooseFile
                }
                GoGlobalSdkRegistry.getInstance().addSdkPath(path)
                setSdk(path)
            }
        }
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    }

    private inner class DownloadSdkAction : AnAction("Download SDK...") {
        override fun actionPerformed(e: AnActionEvent) {
            var versions: List<String> = emptyList()
            val success = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                versions = GoSdkDownloader.fetchVersions()
            }, "Fetching Go Versions", true, project)

            if (!success) return

            if (versions.isEmpty()) {
                Messages.showErrorDialog(project, "Failed to fetch Go versions.", "Download Error")
                return
            }
            val selectedVersion = Messages.showEditableChooseDialog(
                "Select version to download:", "Download Go SDK",
                null, versions.toTypedArray(), versions.first(), null
            )
            if (selectedVersion != null) {
                ProgressManager.getInstance().run(object : Task.Modal(project, "Downloading Go SDK", true) {
                    override fun run(indicator: ProgressIndicator) {
                        val path = GoSdkDownloader.downloadSdk(selectedVersion, indicator)
                        if (path != null) {
                            ApplicationManager.getApplication().invokeLater { setSdk(path) }
                        } else {
                            ApplicationManager.getApplication().invokeLater {
                                Messages.showErrorDialog(project, "Failed to download Go SDK.", "Download Error")
                            }
                        }
                    }
                })
            }
        }
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    }

    override fun getComponent(): JComponent = label
    override fun ID(): String = "GoSdkStatusBarWidget"
    override fun install(statusBar: StatusBar) {}
    override fun dispose() {}
}
