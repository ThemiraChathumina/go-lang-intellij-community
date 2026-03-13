package com.github.themirachathumina.golangintellijcommunity.wizard

import com.github.themirachathumina.golangintellijcommunity.sdk.GoGlobalSdkRegistry
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkDownloader
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.icons.AllIcons
import java.io.File
import javax.swing.DefaultComboBoxModel

import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState

import com.github.themirachathumina.golangintellijcommunity.sdk.GoGlobalSdkRegistryListener

class GoSettingsStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
    private val LOG = Logger.getInstance(GoSettingsStep::class.java)

    private val sdkProperty = propertyGraph.property<String?>(null)
    private var sdk by sdkProperty

    private val sdkModel = DefaultComboBoxModel<String>()
    private val sdkComboBox = ComboBox(sdkModel)
    private val loader = AsyncProcessIcon("GoVersionFetch").apply { isVisible = false }

    init {
        LOG.info("[Go-Lang-Community] Initializing GoSettingsStep")
        sdkComboBox.renderer = textListCellRenderer {
            it?.let { path -> "Go ${GoSdkUtils.retrieveGoVersion(path) ?: ""} ($path)" } ?: "No SDK"
        }
        
        refreshSdkList()

        ApplicationManager.getApplication().messageBus.connect(context.disposable).subscribe(GoGlobalSdkRegistry.TOPIC, object : GoGlobalSdkRegistryListener {
            override fun sdkRegistryChanged() {
                ApplicationManager.getApplication().invokeLater({
                    refreshSdkList()
                }, ModalityState.any())
            }
        })
    }

    private fun refreshSdkList(selectedPath: String? = null) {
        sdkModel.removeAllElements()
        val paths = GoGlobalSdkRegistry.getInstance().getSdkPaths().sortedByDescending { GoSdkUtils.retrieveGoVersion(it) ?: "" }
        paths.forEach { sdkModel.addElement(it) }
        
        if (selectedPath != null && paths.contains(selectedPath)) {
            sdkModel.selectedItem = selectedPath
            sdk = selectedPath
        } else if (paths.isNotEmpty()) {
            sdkModel.selectedItem = paths.first()
            sdk = paths.first()
        }
    }

    override fun setupUI(builder: Panel) {
        with(builder) {
            row("Go SDK:") {
                cell(sdkComboBox)
                    .bindItem(::sdk)
                    .align(AlignX.FILL)
                    .validationOnApply {
                        val projectDir = File(context.projectFileDirectory)
                        if (projectDir.exists() && projectDir.list()?.isNotEmpty() == true) {
                            error("A project or non-empty directory already exists at this location.")
                        } else {
                            null
                        }
                    }

                button("") {
                    val group = DefaultActionGroup()
                    
                    group.add(object : AnAction("Add from Disk...") {
                        override fun actionPerformed(e: AnActionEvent) {
                            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            descriptor.title = "Select Go SDK Directory"
                            FileChooser.chooseFile(descriptor, null, null) { file ->
                                val path = file.path
                                if (GoSdkUtils.isValidSdkHome(path)) {
                                    GoGlobalSdkRegistry.getInstance().addSdkPath(path)
                                    refreshSdkList(path)
                                } else {
                                    Messages.showErrorDialog("The selected directory is not a valid Go SDK.", "Invalid SDK")
                                }
                            }
                        }
                        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
                    })

                    group.add(object : AnAction("Download...") {
                        override fun actionPerformed(e: AnActionEvent) {
                            loader.isVisible = true
                            loader.resume()

                            val modalityState = ModalityState.current()

                            ApplicationManager.getApplication().executeOnPooledThread {
                                val versions = GoSdkDownloader.fetchVersions()
                                
                                ApplicationManager.getApplication().invokeLater({
                                    loader.isVisible = false
                                    loader.suspend()

                                    if (versions.isEmpty()) {
                                        Messages.showErrorDialog("Failed to fetch Go versions.", "Download Error")
                                        return@invokeLater
                                    }

                                    val selectedVersion = Messages.showEditableChooseDialog(
                                        "Select version to download:",
                                        "Download Go SDK",
                                        null,
                                        versions.toTypedArray(),
                                        versions.first(),
                                        null
                                    )

                                    if (selectedVersion != null) {
                                        ProgressManager.getInstance().run(object : Task.Modal(null, "Downloading Go SDK", true) {
                                            override fun run(indicator: ProgressIndicator) {
                                                val path = GoSdkDownloader.downloadSdk(selectedVersion, indicator)
                                                if (path != null) {
                                                    ApplicationManager.getApplication().invokeLater({
                                                        refreshSdkList(path)
                                                    }, ModalityState.any())
                                                } else {
                                                    ApplicationManager.getApplication().invokeLater({
                                                        Messages.showErrorDialog("Failed to download Go SDK.", "Download Error")
                                                    }, ModalityState.any())
                                                }
                                            }
                                        })
                                    }
                                }, modalityState)
                            }
                        }
                        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
                    })

                    val source = it.source as javax.swing.JComponent
                    val popup = JBPopupFactory.getInstance().createActionGroupPopup(
                        null,
                        group,
                        com.intellij.ide.DataManager.getInstance().getDataContext(source),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        false
                    )
                    popup.showUnderneathOf(source)
                }.applyToComponent {
                    icon = AllIcons.General.Add
                }

                cell(loader)
            }
        }
    }

    override fun setupProject(project: Project) {
        LOG.info("[Go-Lang-Community] Setting up project: ${project.name}")
        com.intellij.openapi.application.runWriteAction {
            val selectedSdk = sdk
            if (selectedSdk != null) {
                GoProjectSettings.getInstance(project).setSdkPath(selectedSdk)
            }

            val rawVersion = selectedSdk?.let { GoSdkUtils.retrieveGoVersion(it) } ?: "1.22"
            val modVersion = rawVersion.split(".").take(2).joinToString(".")

            val projectDir = context.projectFileDirectory

            try {
                val root = File(projectDir)
                if (!root.exists()) {
                    root.mkdirs()
                }

                val name = context.projectName
                val goModFile = File(root, "go.mod")
                if (!goModFile.exists()) {
                    goModFile.writeText("module $name\n\ngo $modVersion\n")
                }

                val mainGoFile = File(root, "main.go")
                if (!mainGoFile.exists()) {
                    mainGoFile.writeText("""
                        package main

                        import "fmt"

                        func main() {
                            fmt.Println("Hello, World!")
                        }
                    """.trimIndent())
                }
            } catch (e: Exception) {
                LOG.error("[Go-Lang-Community] Failed to initialize project files", e)
            }
        }
    }
}
