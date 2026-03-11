package com.github.themirachathumina.golangintellijcommunity.wizard

import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkType
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import java.io.File
import javax.swing.DefaultComboBoxModel

class GoSettingsStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
    private val LOG = Logger.getInstance(GoSettingsStep::class.java)

    private val sdkProperty = propertyGraph.property<Sdk?>(null)
    private var sdk by sdkProperty

    // 1. Create a single, permanent model. NEVER replace this object!
    private val sdkModel = DefaultComboBoxModel<Sdk>()

    // 2. Inject the permanent model into the ComboBox immediately
    private val sdkComboBox = ComboBox(sdkModel)

    private var isDetecting = false

    init {
        LOG.info("[Go-Lang-Community] Initializing GoSettingsStep")
        val goSdkType = GoSdkType.getInstance()
        val existingSdks = ProjectJdkTable.getInstance().getSdksOfType(goSdkType)

        // 3. Set the renderer to handle our dynamic text
        sdkComboBox.renderer = textListCellRenderer {
            it?.name ?: if (isDetecting) "Detecting Go SDK..." else "No SDK"
        }

        if (existingSdks.isNotEmpty()) {
            existingSdks.forEach { sdkModel.addElement(it) }
            val bestSdk = existingSdks.maxByOrNull { it.versionString ?: "" }
            sdkModel.selectedItem = bestSdk
            sdk = bestSdk
            LOG.info("[Go-Lang-Community] Using best existing Go SDK: ${sdk?.name}")
        } else {
            fetchSdkInBackground(goSdkType, null)
        }
    }

    private fun fetchSdkInBackground(goSdkType: GoSdkType, manualPath: String?) {
        isDetecting = true
        sdkComboBox.isEnabled = false

        // Clear the model safely. This triggers the "Detecting..." text without breaking bindings!
        sdkModel.removeAllElements()

        com.intellij.openapi.application.ApplicationManager.getApplication().executeOnPooledThread {
            LOG.info("[Go-Lang-Community] Running background SDK detection...")

            val targetPath = manualPath ?: GoSdkUtils.suggestSdkHomePaths().firstOrNull()

            if (targetPath != null) {
                // Prime the cache safely in the background
                GoSdkUtils.retrieveGoVersion(targetPath)

                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
                        val newSdk = SdkConfigurationUtil.createAndAddSDK(targetPath, goSdkType)

                        // 4. THE MAGIC REFRESH: Mutate the existing model!
                        val allSdks = ProjectJdkTable.getInstance().getSdksOfType(goSdkType)

                        sdkModel.removeAllElements()
                        allSdks.forEach { sdkModel.addElement(it) }

                        if (newSdk != null) {
                            sdkModel.selectedItem = newSdk
                            sdk = newSdk
                        }

                        // Unlock the UI
                        isDetecting = false
                        sdkComboBox.isEnabled = true
                    }
                }
            } else {
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    isDetecting = false
                    sdkComboBox.isEnabled = true
                }
            }
        }
    }

    override fun setupUI(builder: Panel) {
        with(builder) {
            row("Go SDK:") {
                val goSdkType = GoSdkType.getInstance()

                cell(sdkComboBox)
                    .bindItem(::sdk)
                    .align(AlignX.FILL)
                    // --- THE FIX: Use native UI DSL validation ---
                    .validationOnApply {
                        val projectDir = File(context.projectFileDirectory)

                        // Strict check: If the directory exists and has files in it, block creation.
                        if (projectDir.exists() && projectDir.list()?.isNotEmpty() == true) {
                            // This throws a UI error and paints a red border around the ComboBox!
                            error("A project or non-empty directory already exists at this location.")
                        } else {
                            null // Validation passed
                        }
                    }

                button("Add SDK...") {
                    SdkConfigurationUtil.selectSdkHome(goSdkType) { selectedPath ->
                        LOG.info("[Go-Lang-Community] User selected SDK home: $selectedPath")
                        fetchSdkInBackground(goSdkType, selectedPath)
                    }
                }
            }
        }
    }

    override fun setupProject(project: Project) {
        LOG.info("[Go-Lang-Community] Setting up project: ${project.name}")
        com.intellij.openapi.application.runWriteAction {
            // 1. Set the SDK
            val selectedSdk = sdk
            if (selectedSdk != null) {
                LOG.info("[Go-Lang-Community] Assigning SDK ${selectedSdk.name} to project ${project.name}")
                ProjectRootManager.getInstance(project).projectSdk = selectedSdk
            }

            // --- THE FIX: Extract the Go version dynamically ---
            // If selectedSdk.versionString is "1.22.5", this safely trims it to "1.22"
            val rawVersion = selectedSdk?.versionString ?: "1.22"
            val modVersion = rawVersion.split(".").take(2).joinToString(".")

            // 2. Initialize Go files in the project directory
            val projectDir = context.projectFileDirectory

            try {
                val root = File(projectDir)
                if (!root.exists()) {
                    LOG.info("[Go-Lang-Community] Creating project directory: $projectDir")
                    root.mkdirs()
                }

                val name = context.projectName
                val goModFile = File(root, "go.mod")
                if (!goModFile.exists()) {
                    LOG.info("[Go-Lang-Community] Creating go.mod for module $name with Go version $modVersion")
                    // Inject the dynamic version here
                    goModFile.writeText("module $name\n\ngo $modVersion\n")
                }

                val mainGoFile = File(root, "main.go")
                if (!mainGoFile.exists()) {
                    LOG.info("[Go-Lang-Community] Creating main.go placeholder")
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
                e.printStackTrace()
            }
        }
    }
}
