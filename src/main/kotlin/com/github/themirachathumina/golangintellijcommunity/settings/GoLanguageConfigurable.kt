package com.github.themirachathumina.golangintellijcommunity.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class GoLanguageConfigurable : BoundConfigurable("Go") {

    private val settings = GoAppSettings.getInstance()

    override fun createPanel(): DialogPanel = panel {
        group("Language Server (gopls)") {
            row {
                checkBox("Enable semantic tokens")
                    .bindSelected(settings.state::semanticTokensEnabled)
                    .comment(
                        "When enabled, gopls provides semantic highlighting (constants, parameters, types, etc.).<br>" +
                        "When disabled, the built-in lexer handles all syntax highlighting.<br>" +
                        "<b>Restart the language server after changing this setting.</b>"
                    )
            }
        }
    }
}
