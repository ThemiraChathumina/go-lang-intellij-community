package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class GoTestSettingsEditor(private val project: Project) : SettingsEditor<GoTestRunConfiguration>() {
    override fun resetEditorFrom(s: GoTestRunConfiguration) {
    }

    override fun applyEditorTo(s: GoTestRunConfiguration) {
    }

    override fun createEditor(): JComponent {
        return panel {
            row("Go Test Configuration") {
                label("Settings will be implemented in Phase 3")
            }
        }
    }
}
