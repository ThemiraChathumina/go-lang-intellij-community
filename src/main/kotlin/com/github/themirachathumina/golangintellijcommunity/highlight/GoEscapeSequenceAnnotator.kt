package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class GoEscapeSequenceAnnotator : Annotator {

    companion object {
        // Matches Go escapes: \a, \b, \f, \n, \r, \t, \v, \\, \', \", \ooo, \xhh, \uhhhh, \Uhhhhhhhh
        private val VALID_ESCAPE_REGEX = Regex("""\\[abfnrtv\\'"]|\\[0-7]{3}|\\x[0-9a-fA-F]{2}|\\u[0-9a-fA-F]{4}|\\U[0-9a-fA-F]{8}""")
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val elementType = element.node.elementType

        // We only care about String and Rune literals
        if (elementType == GoTypes.STRING_LIT || elementType == GoTypes.RUNE_LIT) {
            val text = element.text

            // CRITICAL: Go raw strings (backticks) do NOT process escape characters.
            if (text.startsWith("`")) return

            // Find and highlight all valid escape sequences
            VALID_ESCAPE_REGEX.findAll(text).forEach { match ->
                val startOffset = element.textRange.startOffset + match.range.first
                val endOffset = element.textRange.startOffset + match.range.last + 1
                val range = TextRange(startOffset, endOffset)

                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(range)
                    .textAttributes(GoSyntaxHighlighter.VALID_STRING_ESCAPE)
                    .create()
            }
        }
    }
}