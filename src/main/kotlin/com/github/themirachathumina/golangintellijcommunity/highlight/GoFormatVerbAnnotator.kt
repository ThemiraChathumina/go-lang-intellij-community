package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/*
 * PSI annotator that highlights fmt-style format verbs (%s, %d, %+v, etc.)
 * inside Go string literals using the VALID_STRING_ESCAPE color attribute.
 */
class GoFormatVerbAnnotator : Annotator {

    companion object {
        // Matches Go format verbs like %s, %d, %+v, %.2f, %05d, and %%
        private val VERB_REGEX = Regex("%[+\\- 0#]*\\d*(?:\\.\\d+)?[a-zA-Z%]")
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // We only care about String Literals
        if (element.node.elementType == GoTypes.STRING_LIT) {
            val text = element.text

            // Find all formatting verbs inside the string
            val matches = VERB_REGEX.findAll(text)

            for (match in matches) {
                // Calculate the exact offset in the editor
                val startOffset = element.textRange.startOffset + match.range.first
                val endOffset = element.textRange.startOffset + match.range.last + 1
                val range = TextRange(startOffset, endOffset)

                // Apply the highlight silently (without showing an error/warning tooltip)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(range)
                    // Use standard IDE coloring for escaped characters/verbs
                    .textAttributes(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
                    .create()
            }
        }
    }
}