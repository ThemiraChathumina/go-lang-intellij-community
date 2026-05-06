package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

/*
 * Swing settings editor for GoTestRunConfiguration. Provides fields for 
 * test kind, scope, target path, working directory, and other test-specific options 
 * within the Run/Debug Configurations dialog.
 */
class GoTestSettingsEditor(private val project: Project) : SettingsEditor<GoTestRunConfiguration>() {

    private val testKindComboBox = ComboBox(GoTestKind.entries.toTypedArray()).apply {
        addActionListener { fireEditorStateChanged() }
    }

    private val scopeComboBox = ComboBox(GoTestScope.entries.toTypedArray()).apply {
        addActionListener {
            updateTargetPathLabel()
            fireEditorStateChanged()
        }
    }

    private val targetPathField = TextFieldWithBrowseButton().apply {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = fireEditorStateChanged()
        })
        addBrowseFolderListener(
            "Select Target", null, project,
            object : FileChooserDescriptor(true, true, false, false, false, false) {
                override fun isFileSelectable(file: VirtualFile?): Boolean {
                    return when (currentScope()) {
                        GoTestScope.FILE -> file != null && !file.isDirectory && file.name.endsWith("_test.go")
                        GoTestScope.DIRECTORY, GoTestScope.PACKAGE -> file != null && file.isDirectory
                        GoTestScope.FUNCTION -> file != null && !file.isDirectory && file.name.endsWith("_test.go")
                    }
                }

                override fun isFileVisible(file: VirtualFile?, showHiddenFiles: Boolean): Boolean {
                    return when (currentScope()) {
                        GoTestScope.DIRECTORY, GoTestScope.PACKAGE -> file != null && (file.isDirectory || showHiddenFiles)
                        else -> super.isFileVisible(file, showHiddenFiles)
                    }
                }
            }
        )
    }

    private val outputDirectoryField = TextFieldWithBrowseButton().apply {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = fireEditorStateChanged()
        })
        addBrowseFolderListener(
            "Select Output Directory", null, project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
    }

    private val workingDirField = TextFieldWithBrowseButton().apply {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = fireEditorStateChanged()
        })
        addBrowseFolderListener(
            "Select Working Directory", null, project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
    }

    private val envVarsComponent = EnvironmentVariablesComponent().apply {
        addChangeListener { fireEditorStateChanged() }
    }

    private val goToolArgsField = RawCommandLineEditor().apply {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = fireEditorStateChanged()
        })
    }

    private val isVerboseCheckBox = JBCheckBox("-v (verbose output)").apply {
        addActionListener { fireEditorStateChanged() }
    }

    private val useAllCustomBuildTagsCheckBox = JBCheckBox("Use all custom build tags").apply {
        addActionListener { fireEditorStateChanged() }
    }

    private val programArgsField = RawCommandLineEditor().apply {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = fireEditorStateChanged()
        })
    }

    private val moduleNameField = JBTextField().apply {
        document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = fireEditorStateChanged()
        })
    }

    private val runWithElevatedPrivilegesCheckBox = JBCheckBox("Run with elevated privileges").apply {
        addActionListener { fireEditorStateChanged() }
    }

    private var targetPathLabel: javax.swing.JLabel? = null

    private fun currentScope(): GoTestScope =
        scopeComboBox.item ?: GoTestScope.FILE

    private fun updateTargetPathLabel() {
        targetPathLabel?.text = when (currentScope()) {
            GoTestScope.FILE -> "Files:"
            GoTestScope.DIRECTORY -> "Directory:"
            GoTestScope.PACKAGE -> "Package:"
            GoTestScope.FUNCTION -> "Function:"
        }
    }

    override fun createEditor(): JComponent {
        return panel {
            row("Test framework:") {
                label("gotest")
            }
            row("Action:") {
                cell(testKindComboBox).align(AlignX.FILL)
            }.comment("Choose between standard tests or benchmarks")
            row("Test kind:") {
                cell(scopeComboBox).align(AlignX.FILL)
            }
            row {
                targetPathLabel = label("Files:").component
                cell(targetPathField).align(AlignX.FILL)
            }
            row("Output directory:") {
                cell(outputDirectoryField).align(AlignX.FILL)
            }
            row("Working directory:") {
                cell(workingDirField).align(AlignX.FILL)
            }
            row("Environment:") {
                cell(envVarsComponent).align(AlignX.FILL)
            }
            row("Go tool arguments:") {
                cell(goToolArgsField).align(AlignX.FILL)
            }.comment("Arguments passed to 'go test', e.g. -count=1 -timeout=30s")
            row {
                cell(isVerboseCheckBox)
            }
            row {
                cell(useAllCustomBuildTagsCheckBox)
            }
            row("Program arguments:") {
                cell(programArgsField).align(AlignX.FILL)
            }.comment("Arguments passed to the test binary")
            row("Module:") {
                cell(moduleNameField).align(AlignX.FILL)
            }.comment("Go module name (auto-detected from go.mod)")
            row {
                cell(runWithElevatedPrivilegesCheckBox)
            }
        }
    }

    override fun resetEditorFrom(config: GoTestRunConfiguration) {
        testKindComboBox.item = config.testKind
        scopeComboBox.item = config.scope
        targetPathField.text = config.targetPath
        outputDirectoryField.text = config.outputDirectory
        workingDirField.text = config.workingDirectory
        envVarsComponent.envs = config.envVariables
        goToolArgsField.text = config.goToolArguments
        isVerboseCheckBox.isSelected = config.isVerbose
        useAllCustomBuildTagsCheckBox.isSelected = config.useAllCustomBuildTags
        programArgsField.text = config.programArguments
        moduleNameField.text = config.moduleName
        runWithElevatedPrivilegesCheckBox.isSelected = config.runWithElevatedPrivileges
        updateTargetPathLabel()
    }

    override fun applyEditorTo(config: GoTestRunConfiguration) {
        config.testKind = testKindComboBox.item ?: GoTestKind.TEST
        config.scope = currentScope()
        config.targetPath = targetPathField.text
        config.outputDirectory = outputDirectoryField.text
        config.workingDirectory = workingDirField.text
        config.envVariables = envVarsComponent.envs
        config.goToolArguments = goToolArgsField.text
        config.isVerbose = isVerboseCheckBox.isSelected
        config.useAllCustomBuildTags = useAllCustomBuildTagsCheckBox.isSelected
        config.programArguments = programArgsField.text
        config.moduleName = moduleNameField.text
        config.runWithElevatedPrivileges = runWithElevatedPrivilegesCheckBox.isSelected
    }
}