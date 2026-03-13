package com.github.themirachathumina.golangintellijcommunity.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiFile
import com.redhat.devtools.lsp4ij.features.semanticTokens.SemanticTokensColorsProvider

/**
 * Maps gopls semantic tokens to IntelliJ colors.
 * Returns null for all tokens except constants (variable + readonly modifier),
 * so the lexer-based syntax highlighter handles everything else.
 */
class GoSemanticTokensColorsProvider : SemanticTokensColorsProvider {

    override fun getTextAttributesKey(
        tokenType: String,
        tokenModifiers: List<String>,
        file: PsiFile
    ): TextAttributesKey? {
        if (tokenType == "variable" && "readonly" in tokenModifiers) {
            return GoSyntaxHighlighter.CONSTANT
        }
        return null
    }
}
