package com.github.themirachathumina.golangintellijcommunity.highlight

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.psi.PsiErrorElement
import org.jetbrains.annotations.NotNull

class GoHighlightErrorFilter : HighlightErrorFilter() {
    override fun shouldHighlightErrorElement(@NotNull element: PsiErrorElement): Boolean {
        // Disable highlighting for PsiErrorElements in Go files
        return element.language.id != "Go"
    }
}
