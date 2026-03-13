package com.github.themirachathumina.golangintellijcommunity.wizard

import com.github.themirachathumina.golangintellijcommunity.sdk.GoGlobalSdkRegistry
import com.github.themirachathumina.golangintellijcommunity.sdk.GoGlobalSdkRegistryListener
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkDownloader
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.icons.AllIcons
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.util.ui.AsyncProcessIcon
import java.io.File
import javax.swing.DefaultComboBoxModel
import javax.swing.JCheckBox
import javax.swing.JTextField

enum class GoProjectType(val displayName: String) {
    EXECUTABLE("Executable (main package)"),
    LIBRARY("Library (reusable package)"),
    CLI("CLI Application (cmd/ structure)")
}

class GoSettingsStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
    private val LOG = Logger.getInstance(GoSettingsStep::class.java)

    // --- SDK ---
    private val sdkProperty = propertyGraph.property<String?>(null)
    private var sdk by sdkProperty
    private val sdkModel = DefaultComboBoxModel<String>()
    private val sdkComboBox = ComboBox(sdkModel)
    private val loader = AsyncProcessIcon("GoVersionFetch").apply { isVisible = false }

    // --- Module path ---
    private val modulePathField = JTextField(30)

    // --- Project type ---
    private val projectTypeProperty = propertyGraph.property<GoProjectType?>(GoProjectType.EXECUTABLE)
    private var projectType by projectTypeProperty
    private val projectTypeModel = DefaultComboBoxModel(GoProjectType.entries.toTypedArray())
    private val projectTypeComboBox = ComboBox(projectTypeModel)

    // --- Workspace ---
    private val workspaceCheckBox = JCheckBox("Initialize as Go workspace (go.work)")

    init {
        sdkComboBox.renderer = textListCellRenderer {
            it?.let { path -> "Go ${GoSdkUtils.retrieveGoVersion(path) ?: ""} ($path)" } ?: "No SDK"
        }

        projectTypeComboBox.renderer = textListCellRenderer {
            it?.displayName ?: ""
        }

        refreshSdkList()

        ApplicationManager.getApplication().messageBus.connect(context.disposable)
            .subscribe(GoGlobalSdkRegistry.TOPIC, object : GoGlobalSdkRegistryListener {
                override fun sdkRegistryChanged() {
                    ApplicationManager.getApplication().invokeLater({ refreshSdkList() }, ModalityState.any())
                }
            })
    }

    private fun refreshSdkList(selectedPath: String? = null) {
        sdkModel.removeAllElements()
        val paths = GoGlobalSdkRegistry.getInstance().getSdkPaths()
            .sortedByDescending { GoSdkUtils.retrieveGoVersion(it) ?: "" }
        paths.forEach { sdkModel.addElement(it) }
        val target = selectedPath ?: paths.firstOrNull()
        if (target != null && paths.contains(target)) {
            sdkModel.selectedItem = target
            sdk = target
        }
    }

    override fun setupUI(builder: Panel) {
        // Pre-fill module path with project name (projectName may be null early in wizard lifecycle)
        if (modulePathField.text.isNullOrBlank()) {
            modulePathField.text = context.projectName?.ifBlank { "mymodule" } ?: "mymodule"
        }

        with(builder) {
            // SDK row
            row("Go SDK:") {
                cell(sdkComboBox)
                    .bindItem(::sdk)
                    .align(AlignX.FILL)

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
                                        "Select version to download:", "Download Go SDK",
                                        null, versions.toTypedArray(), versions.first(), null
                                    )
                                    if (selectedVersion != null) {
                                        ProgressManager.getInstance().run(object : Task.Modal(null, "Downloading Go SDK", true) {
                                            override fun run(indicator: ProgressIndicator) {
                                                val path = GoSdkDownloader.downloadSdk(selectedVersion, indicator)
                                                ApplicationManager.getApplication().invokeLater({
                                                    if (path != null) refreshSdkList(path)
                                                    else Messages.showErrorDialog("Failed to download Go SDK.", "Download Error")
                                                }, ModalityState.any())
                                            }
                                        })
                                    }
                                }, modalityState)
                            }
                        }
                        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
                    })
                    val source = it.source as javax.swing.JComponent
                    JBPopupFactory.getInstance().createActionGroupPopup(
                        null, group,
                        com.intellij.ide.DataManager.getInstance().getDataContext(source),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false
                    ).showUnderneathOf(source)
                }.applyToComponent { icon = AllIcons.General.Add }

                cell(loader)
            }

            // Module path row
            row("Module path:") {
                cell(modulePathField)
                    .align(AlignX.FILL)
                    .validationOnApply {
                        if (modulePathField.text.isBlank()) error("Module path cannot be empty")
                        else null
                    }
            }

            // Project type row
            row("Project type:") {
                cell(projectTypeComboBox)
                    .bindItem(::projectType)
                    .align(AlignX.FILL)
            }

            // Workspace checkbox
            row("") {
                cell(workspaceCheckBox)
            }
        }
    }

    override fun setupProject(project: Project) {
        val modulePath = modulePathField.text.trim()
        val initWorkspace = workspaceCheckBox.isSelected

        LOG.info("[Go-Lang-Community] Setting up project: ${project.name}, type=${projectType?.displayName}, module=$modulePath")

        com.intellij.openapi.application.runWriteAction {
            val selectedSdk = sdk
            if (selectedSdk != null) GoProjectSettings.getInstance(project).setSdkPath(selectedSdk)

            val rawVersion = selectedSdk?.let { GoSdkUtils.retrieveGoVersion(it) } ?: "1.21"
            val modVersion = rawVersion.split(".").take(2).joinToString(".")
            val effectiveModule = modulePath.ifBlank { context.projectName }
            val projectDir = File(context.projectFileDirectory).also { if (!it.exists()) it.mkdirs() }

            try {
                // go.mod
                File(projectDir, "go.mod").takeIf { !it.exists() }
                    ?.writeText("module $effectiveModule\n\ngo $modVersion\n")

                // Source files based on project type
                when (projectType ?: GoProjectType.EXECUTABLE) {
                    GoProjectType.EXECUTABLE -> scaffoldExecutable(projectDir)
                    GoProjectType.LIBRARY    -> scaffoldLibrary(projectDir, project.name)
                    GoProjectType.CLI        -> scaffoldCli(projectDir, project.name, effectiveModule)
                }

                // go.work
                if (initWorkspace) {
                    File(projectDir, "go.work").takeIf { !it.exists() }
                        ?.writeText("go $modVersion\n\nuse .\n")
                }
            } catch (e: Exception) {
                LOG.error("[Go-Lang-Community] Failed to initialize project files", e)
            }
        }
    }

    // --- Scaffolding helpers ---

    private fun scaffoldExecutable(root: File) {
        File(root, "main.go").takeIf { !it.exists() }?.writeText(
            """
            package main

            import "fmt"

            func main() {
                fmt.Println("Hello, World!")
            }
            """.trimIndent() + "\n"
        )
    }

    private fun scaffoldLibrary(root: File, projectName: String) {
        val pkgName = toGoIdentifier(projectName)
        File(root, "$pkgName.go").takeIf { !it.exists() }?.writeText(
            """
            package $pkgName

            // Hello returns a greeting for the given name.
            func Hello(name string) string {
                return "Hello, " + name + "!"
            }
            """.trimIndent() + "\n"
        )
    }

    private fun scaffoldCli(root: File, projectName: String, modulePath: String) {
        val pkgName = toGoIdentifier(projectName)

        // cmd/<name>/main.go
        val cmdDir = File(root, "cmd/$pkgName").also { it.mkdirs() }
        File(cmdDir, "main.go").takeIf { !it.exists() }?.writeText(
            """
            package main

            import (
                "fmt"
                "$modulePath/internal/$pkgName"
            )

            func main() {
                fmt.Println($pkgName.Greeting())
            }
            """.trimIndent() + "\n"
        )

        // internal/<name>/<name>.go
        val internalDir = File(root, "internal/$pkgName").also { it.mkdirs() }
        File(internalDir, "$pkgName.go").takeIf { !it.exists() }?.writeText(
            """
            package $pkgName

            // Greeting returns the application greeting.
            func Greeting() string {
                return "Hello from $projectName!"
            }
            """.trimIndent() + "\n"
        )
    }

    /** Converts a project name to a valid Go package identifier (lowercase, underscores). */
    private fun toGoIdentifier(name: String): String =
        name.lowercase().replace(Regex("[^a-z0-9_]"), "_").trimStart('_').ifBlank { "pkg" }
}
