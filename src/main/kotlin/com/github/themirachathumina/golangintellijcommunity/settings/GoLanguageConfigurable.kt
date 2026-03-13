package com.github.themirachathumina.golangintellijcommunity.settings

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

    override fun createPanel(): DialogPanel = panel {
        group("Language Server (gopls)") {
            var lspCheck: Cell<JCheckBox>? = null
            row {
                lspCheck = checkBox("Enable Go language server (gopls)")
                    .bindSelected(settings.state::lspEnabled)
                    .comment("Start or stop gopls for this IDE session. Takes effect on Apply.")
            }
            row {
                checkBox("Enable semantic tokens")
                    .bindSelected(settings.state::semanticTokensEnabled)
                    .enabledIf(@Suppress("UNCHECKED_CAST") (lspCheck!! as Cell<AbstractButton>).selected)
                    .comment(
                        "When enabled, gopls provides semantic highlighting (constants, etc.).<br>" +
                        "Takes effect on Apply (restarts the language server)."
                    )
            }
        }
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
                !isLspEnabled -> manager.stop("gopls")
                !wasLspEnabled -> manager.start("gopls")
                wasSemanticEnabled != isSemanticEnabled -> {
                    manager.stop("gopls")
                    manager.start("gopls")
                }
            }
        }
    }
}
