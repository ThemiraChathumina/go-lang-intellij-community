package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class GoRunSettingsEditor(private val project: Project) : SettingsEditor<GoRunConfiguration>() {

    private val runKindComboBox = ComboBox(GoRunKind.values())
    private val targetPathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener("Select Target", null, project, FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor())
    }
    private val workingDirField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener("Select Working Directory", null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor())
    }
    private val programArgsField = RawCommandLineEditor()
    private val goToolArgsField = RawCommandLineEditor()
    private val envVarsComponent = EnvironmentVariablesComponent()

    override fun createEditor(): JComponent {
        return panel {
            row("Run kind:") { cell(runKindComboBox).align(AlignX.FILL) }
            row("File/Package:") { cell(targetPathField).align(AlignX.FILL) }
            row("Working directory:") { cell(workingDirField).align(AlignX.FILL) }
            row("Program arguments:") { cell(programArgsField).align(AlignX.FILL) }
            row("Go tool arguments:") { cell(goToolArgsField).align(AlignX.FILL) }
            row("Environment:") { cell(envVarsComponent).align(AlignX.FILL) }
        }
    }

    // Pushes data FROM the configuration object TO the UI fields
    override fun resetEditorFrom(config: GoRunConfiguration) {
        runKindComboBox.item = config.runKind
        targetPathField.text = config.targetPath
        workingDirField.text = config.workingDirectory
        programArgsField.text = config.programArguments
        goToolArgsField.text = config.goToolArguments
        envVarsComponent.envs = config.envVariables
    }

    // Pulls data FROM the UI fields TO the configuration object
    override fun applyEditorTo(config: GoRunConfiguration) {
        config.runKind = runKindComboBox.item
        config.targetPath = targetPathField.text
        config.workingDirectory = workingDirField.text
        config.programArguments = programArgsField.text
        config.goToolArguments = goToolArgsField.text
        config.envVariables = envVarsComponent.envs
    }
}