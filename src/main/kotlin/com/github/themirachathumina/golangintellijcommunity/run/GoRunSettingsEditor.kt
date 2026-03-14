package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel

class GoRunSettingsEditor(private val project: Project) : SettingsEditor<GoRunConfiguration>() {

    companion object {
        private val GOOS_VALUES = listOf("", "linux", "windows", "darwin", "freebsd", "netbsd", "openbsd", "plan9", "solaris", "android", "dragonfly", "illumos", "js", "aix")
        private val GOARCH_VALUES = listOf("", "amd64", "386", "arm", "arm64", "ppc64", "ppc64le", "mips", "mipsle", "mips64", "mips64le", "s390x", "wasm", "riscv64")
    }

    private var currentKind = GoRunKind.FILE

    private val fileRadioButton = JBRadioButton("File", true).apply {
        addActionListener { updateUI() }
    }
    private val packageRadioButton = JBRadioButton("Package").apply {
        addActionListener { updateUI() }
    }
    private val kindGroup = ButtonGroup().apply {
        add(fileRadioButton)
        add(packageRadioButton)
    }

    private val targetPathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener("Select Target", null, project, object : FileChooserDescriptor(true, true, false, false, false, false) {
            override fun isFileSelectable(file: VirtualFile?): Boolean {
                return if (currentKind == GoRunKind.FILE) {
                    file != null && !file.isDirectory && file.extension == "go"
                } else {
                    file != null && file.isDirectory
                }
            }

            override fun isFileVisible(file: VirtualFile?, showHiddenFiles: Boolean): Boolean {
                if (currentKind == GoRunKind.PACKAGE) {
                    return file != null && (file.isDirectory || showHiddenFiles)
                }
                return super.isFileVisible(file, showHiddenFiles)
            }
        })
    }

    private val workingDirField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener("Select Working Directory", null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor())
    }
    private val programArgsField = RawCommandLineEditor()
    private val goToolArgsField = RawCommandLineEditor()
    private val envVarsComponent = EnvironmentVariablesComponent()

    // New Build Options
    private val enableRaceCheckBox = JBCheckBox("Enable race detector (-race)")
    private val buildTagsField = JBTextField()
    private val buildModeComboBox = ComboBox(GoBuildMode.entries.toTypedArray())
    
    // More Options
    private val goOsComboBox = ComboBox(GOOS_VALUES.toTypedArray())
    private val goArchComboBox = ComboBox(GOARCH_VALUES.toTypedArray())
    private val cgoEnabledCheckBox = JBCheckBox("Enable CGO")
    private val outputPathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener("Select Output Path", null, project, FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor())
    }
    private val goProxyField = JBTextField()
    private val stdinFilePathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener("Select Stdin File", null, project, FileChooserDescriptorFactory.createSingleFileDescriptor())
    }
    private val timeoutSecondsSpinner = JBIntSpinner(0, 0, 3600)
    private val runInExternalTerminalCheckBox = JBCheckBox("Run in external terminal")

    private var targetPathLabel: JLabel? = null

    override fun createEditor(): JComponent {
        return panel {
            group("Configuration") {
                buttonsGroup("Run kind:") {
                    row {
                        cell(fileRadioButton)
                        cell(packageRadioButton)
                    }
                }
                row {
                    targetPathLabel = label("Go file:").component
                    cell(targetPathField).align(AlignX.FILL)
                }
                row("Working directory:") {
                    cell(workingDirField).align(AlignX.FILL)
                }.comment("Directory the process runs from. Defaults to the project root.")
            }

            group("Arguments") {
                row("Program arguments:") {
                    cell(programArgsField).align(AlignX.FILL)
                }.comment("Arguments passed to the binary, e.g. --port 8080")

                row("Go tool arguments:") {
                    cell(goToolArgsField).align(AlignX.FILL)
                }.comment("Arguments passed to 'go build', e.g. -v -count=1")

                row("Environment:") {
                    cell(envVarsComponent).align(AlignX.FILL)
                }
            }

            group("Build Options") {
                row {
                    cell(enableRaceCheckBox)
                }
                row("Build tags:") {
                    cell(buildTagsField).align(AlignX.FILL)
                }.comment("e.g. dev,integration")
                row("Build mode:") {
                    cell(buildModeComboBox).align(AlignX.FILL)
                }

                collapsibleGroup("More options") {
                    row("GOOS:") { cell(goOsComboBox).align(AlignX.FILL) }
                    row("GOARCH:") { cell(goArchComboBox).align(AlignX.FILL) }
                    row { cell(cgoEnabledCheckBox) }
                    row("Output binary:") {
                        cell(outputPathField).align(AlignX.FILL)
                    }
                    row("GOPROXY:") {
                        cell(goProxyField).align(AlignX.FILL)
                    }.comment("e.g. off, direct, https://proxy.corp")
                    row("Redirect stdin from file:") {
                        cell(stdinFilePathField).align(AlignX.FILL)
                    }
                    row("Timeout (seconds):") {
                        cell(timeoutSecondsSpinner)
                    }
                    row { cell(runInExternalTerminalCheckBox) }
                }
            }
        }
    }

    private fun updateUI() {
        currentKind = if (fileRadioButton.isSelected) GoRunKind.FILE else GoRunKind.PACKAGE
        targetPathLabel?.text = if (currentKind == GoRunKind.FILE) "Go file:" else "Package directory:"
    }

    // Pushes data FROM the configuration object TO the UI fields
    override fun resetEditorFrom(config: GoRunConfiguration) {
        currentKind = config.runKind
        if (currentKind == GoRunKind.FILE) {
            fileRadioButton.isSelected = true
        } else {
            packageRadioButton.isSelected = true
        }
        
        targetPathField.text = config.targetPath
        workingDirField.text = config.workingDirectory
        programArgsField.text = config.programArguments
        goToolArgsField.text = config.goToolArguments
        envVarsComponent.envs = config.envVariables

        // New fields
        enableRaceCheckBox.isSelected = config.enableRace
        buildTagsField.text = config.buildTags
        buildModeComboBox.item = config.buildMode
        goOsComboBox.item = config.goOS
        goArchComboBox.item = config.goArch
        cgoEnabledCheckBox.isSelected = config.cgoEnabled
        outputPathField.text = config.outputPath
        goProxyField.text = config.goProxy
        stdinFilePathField.text = config.stdinFilePath
        timeoutSecondsSpinner.value = config.timeoutSeconds
        runInExternalTerminalCheckBox.isSelected = config.runInExternalTerminal
        
        updateUI()
    }

    // Pulls data FROM the UI fields TO the configuration object
    override fun applyEditorTo(config: GoRunConfiguration) {
        config.runKind = if (fileRadioButton.isSelected) GoRunKind.FILE else GoRunKind.PACKAGE
        config.targetPath = targetPathField.text
        config.workingDirectory = workingDirField.text
        config.programArguments = programArgsField.text
        config.goToolArguments = goToolArgsField.text
        config.envVariables = envVarsComponent.envs

        // New fields
        config.enableRace = enableRaceCheckBox.isSelected
        config.buildTags = buildTagsField.text
        config.buildMode = buildModeComboBox.item
        config.goOS = goOsComboBox.item ?: ""
        config.goArch = goArchComboBox.item ?: ""
        config.cgoEnabled = cgoEnabledCheckBox.isSelected
        config.outputPath = outputPathField.text
        config.goProxy = goProxyField.text
        config.stdinFilePath = stdinFilePathField.text
        config.timeoutSeconds = timeoutSecondsSpinner.number
        config.runInExternalTerminal = runInExternalTerminalCheckBox.isSelected
    }
}
