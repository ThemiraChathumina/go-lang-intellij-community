package com.github.themirachathumina.golangintellijcommunity.highlight

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.psi.PsiErrorElement
import org.jetbrains.annotations.NotNull

/*
 * Suppresses IntelliJ's default PsiErrorElement highlighting for Go files.
 * Go error recovery is handled by gopls (LSP), so PSI parse errors should not appear as red squiggles.
 */
class GoHighlightErrorFilter : HighlightErrorFilter() {
    override fun shouldHighlightErrorElement(@NotNull element: PsiErrorElement): Boolean {
        // Disable highlighting for PsiErrorElements in Go files
        return element.language.id != "Go"
    }
}
