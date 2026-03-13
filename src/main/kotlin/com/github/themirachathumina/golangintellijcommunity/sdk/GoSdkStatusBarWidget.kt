package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.sdk.GoGlobalSdkRegistryListener
import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooser
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

import com.intellij.openapi.application.ModalityState

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
                ApplicationManager.getApplication().invokeLater({
                    updateLabel()
                }, ModalityState.any())
            }
        })

        ApplicationManager.getApplication().messageBus.connect(this).subscribe(GoGlobalSdkRegistry.TOPIC, object : GoGlobalSdkRegistryListener {
            override fun sdkRegistryChanged() {
                ApplicationManager.getApplication().invokeLater({
                    updateLabel()
                }, ModalityState.any())
            }
        })
    }

    private fun updateLabel() {
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        if (sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)) {
            val version = GoSdkUtils.retrieveGoVersion(sdkPath)
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

        // Pre-fetch versions on a pooled thread so the cache is warm before building the popup
        val versionMap = mutableMapOf<String, String>()
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            for (path in paths) {
                val version = GoSdkUtils.retrieveGoVersion(path)
                if (version != null) {
                    versionMap[path] = version
                }
            }
        }, "Loading Go SDK Versions", false, project)

        val sortedPaths = paths.sortedByDescending { versionMap[it] ?: "" }

        val group = DefaultActionGroup()

        for (path in sortedPaths) {
            val version = versionMap[path] ?: "Unknown"
            group.add(object : AnAction("Go $version ($path)") {
                override fun actionPerformed(e: AnActionEvent) {
                    setSdk(path)
                }
                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
            })
        }
        
        group.addSeparator()
        
        group.add(object : AnAction("Add Local SDK...") {
            override fun actionPerformed(e: AnActionEvent) {
                val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                descriptor.title = "Select Go SDK Directory"
                FileChooser.chooseFile(descriptor, project, null) { file ->
                    val path = file.path
                    if (!GoSdkUtils.isValidSdkHome(path)) {
                        Messages.showErrorDialog(project, "The selected directory is not a valid Go SDK.", "Invalid SDK")
                        return@chooseFile
                    }
                    val version = GoSdkUtils.retrieveGoVersion(path)
                    if (version != null && !GoSdkUtils.isSupportedVersion(version)) {
                        Messages.showErrorDialog(project, "Go $version is not supported. Minimum required version is Go ${GoSdkUtils.MIN_SUPPORTED_VERSION}.", "Unsupported SDK Version")
                        return@chooseFile
                    }
                    registry.addSdkPath(path)
                    setSdk(path)
                }
            }
            override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
        })

        group.add(object : AnAction("Download SDK...") {
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
                val selectedVersion = Messages.showEditableChooseDialog("Select version to download:", "Download Go SDK", null, versions.toTypedArray(), versions.first(), null)
                if (selectedVersion != null) {
                    ProgressManager.getInstance().run(object : Task.Modal(project, "Downloading Go SDK", true) {
                        override fun run(indicator: ProgressIndicator) {
                            val path = GoSdkDownloader.downloadSdk(selectedVersion, indicator)
                            if (path != null) {
                                ApplicationManager.getApplication().invokeLater {
                                    setSdk(path)
                                }
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
        })

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
        val settings = GoProjectSettings.getInstance(project)
        settings.setSdkPath(path)
        
        GoModReconciler.reconcile(project, path)
        
        // Restart LSP
        ApplicationManager.getApplication().invokeLater {
            LanguageServerManager.getInstance(project).stop("gopls")
            LanguageServerManager.getInstance(project).start("gopls")
        }
    }

    override fun getComponent(): JComponent = label

    override fun ID(): String = "GoSdkStatusBarWidget"

    override fun install(statusBar: StatusBar) {}

    override fun dispose() {}
}
