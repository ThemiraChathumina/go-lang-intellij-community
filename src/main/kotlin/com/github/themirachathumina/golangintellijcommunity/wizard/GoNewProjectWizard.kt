package com.github.themirachathumina.golangintellijcommunity.wizard

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkType
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import java.io.File
import javax.swing.Icon

class GoNewProjectWizard : GeneratorNewProjectWizard {
    override val id: String = "Go"
    override val name: String = "Go"
    override val icon: Icon = GoIcons.FILE

    override fun createStep(context: WizardContext): NewProjectWizardStep {
        return RootNewProjectWizardStep(context)
            .chain(::NewProjectWizardBaseStep, ::GitNewProjectWizardStep, ::GoSettingsStep)
    }
}

class GoSettingsStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
    private val sdkProperty = propertyGraph.property<Sdk?>(null)
    private var sdk by sdkProperty

    init {
        val goSdkType = GoSdkType.getInstance()
        val existingSdks = ProjectJdkTable.getInstance().getSdksOfType(goSdkType)
        if (existingSdks.isNotEmpty()) {
            sdk = existingSdks.maxByOrNull { it.versionString ?: "" }
        } else {
            val suggested = GoSdkUtils.suggestSdkHomePaths().firstOrNull()
            if (suggested != null) {
                sdk = SdkConfigurationUtil.createAndAddSDK(suggested, goSdkType)
            }
        }
    }

    override fun setupUI(builder: Panel) {
        with(builder) {
            row("Go SDK:") {
                val goSdkType = GoSdkType.getInstance()
                comboBox(ProjectJdkTable.getInstance().getSdksOfType(goSdkType).toList(), textListCellRenderer { 
                    it?.name ?: "No SDK"
                })
                .bindItem(sdkProperty)
                .align(AlignX.FILL)
                
                button("Add SDK...") {
                    SdkConfigurationUtil.selectSdkHome(goSdkType) { selectedPath ->
                        val newSdk = SdkConfigurationUtil.createAndAddSDK(selectedPath, goSdkType)
                        if (newSdk != null) {
                            sdk = newSdk
                        }
                    }
                }
            }
        }
    }

    override fun setupProject(project: Project) {
        com.intellij.openapi.application.runWriteAction {
            // 1. Set the SDK
            val selectedSdk = sdk
            if (selectedSdk != null) {
                ProjectRootManager.getInstance(project).projectSdk = selectedSdk
            }

            val projectDir = context.projectFileDirectory

            try {
                val root = File(projectDir)
                if (!root.exists()) root.mkdirs()

                val name = context.projectName

                // --- FIX 2: Sanitize the module name ---
                // Replaces spaces and invalid characters with hyphens, and makes it lowercase
                val sanitizedModuleName = name.replace(Regex("[^a-zA-Z0-9.-]"), "-").lowercase()

                // --- FIX 1: Extract Go version from the selected SDK ---
                var goVersion = "1.22" // Fallback
                val versionString = selectedSdk?.versionString
                if (versionString != null) {
                    // SDK version strings usually look like "go1.22.1" or "1.21.0"
                    // We only want the "1.22" or "1.21" part for the go.mod file
                    val match = Regex("""(?:go)?(\d+\.\d+)""").find(versionString)
                    if (match != null) {
                        goVersion = match.groupValues[1]
                    }
                }

                val goModFile = File(root, "go.mod")
                if (!goModFile.exists()) {
                    goModFile.writeText("module $sanitizedModuleName\n\ngo $goVersion\n")
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

                // --- FIX 3: Notify IntelliJ's Virtual File System ---
                // This ensures the Project View shows the files immediately
                com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshIoFiles(listOf(root, goModFile, mainGoFile))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
