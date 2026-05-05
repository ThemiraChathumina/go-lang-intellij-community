package com.github.themirachathumina.golangintellijcommunity.settings

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.redhat.devtools.lsp4ij.LanguageServerManager
import javax.swing.AbstractButton
import javax.swing.JCheckBox

class GoLanguageConfigurable : BoundConfigurable("Go") {

    private val settings = GoAppSettings.getInstance()

    override fun createPanel(): DialogPanel {
        var lspCheck: Cell<JCheckBox>? = null
        var semanticCheck: Cell<JCheckBox>? = null
        var logCheck: Cell<JCheckBox>? = null

        val dialogPanel = panel {
            group("Language Server (gopls)") {
                row {
                    lspCheck = checkBox("Enable Go language server (gopls)")
                        .bindSelected(settings.state::lspEnabled)
                        .comment("Start or stop gopls for this IDE session. Takes effect on Apply.")
                }
                row {
                    semanticCheck = checkBox("Enable semantic tokens")
                        .bindSelected(settings.state::semanticTokensEnabled)
                        .enabledIf(@Suppress("UNCHECKED_CAST") (lspCheck!! as Cell<AbstractButton>).selected)
                        .comment(
                            "When enabled, gopls provides semantic highlighting (constants, etc.).<br>" +
                            "Takes effect on Apply (restarts the language server)."
                        )
                }
                row {
                    logCheck = checkBox("Log semantic tokens to stdout")
                        .bindSelected(settings.state::logSemanticTokens)
                        .enabledIf(@Suppress("UNCHECKED_CAST") (semanticCheck!! as Cell<AbstractButton>).selected)
                        .comment("Print decoded semantic token output to the Gradle runIde console for debugging.")
                }
            }
        }

        val lspC      = lspCheck!!
        val semanticC = semanticCheck!!
        val logC      = logCheck!!

        // When gopls is unchecked, force-uncheck both dependent checkboxes.
        lspC.component.addActionListener {
            if (!lspC.component.isSelected) {
                semanticC.component.isSelected = false
                logC.component.isSelected = false
            }
        }
        // When semantic tokens is unchecked, force-uncheck the log checkbox.
        semanticC.component.addActionListener {
            if (!semanticC.component.isSelected) {
                logC.component.isSelected = false
            }
        }

        return dialogPanel
    }

    override fun apply() {
        val wasLspEnabled = settings.state.lspEnabled
        val wasSemanticEnabled = settings.state.semanticTokensEnabled
        super.apply()
        val isLspEnabled = settings.state.lspEnabled
        val isSemanticEnabled = settings.state.semanticTokensEnabled

        for (project in ProjectManager.getInstance().openProjects) {
            val manager = LanguageServerManager.getInstance(project)
            when {
                !isLspEnabled -> manager.stop(GoConstants.Lsp.SERVER_ID)
                !wasLspEnabled -> manager.start(GoConstants.Lsp.SERVER_ID)
                wasSemanticEnabled != isSemanticEnabled -> {
                    manager.stop(GoConstants.Lsp.SERVER_ID)
                    manager.start(GoConstants.Lsp.SERVER_ID)
                }
            }
        }
    }
}
