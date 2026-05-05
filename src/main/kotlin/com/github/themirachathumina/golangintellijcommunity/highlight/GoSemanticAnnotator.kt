package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.psi.GoTag
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * PSI-based annotator for struct tag key highlighting — regex-based, no LSP semantic token equivalent.
 *
 * Import path STRING override is handled by GoImportStringAnnotator (ExternalAnnotator),
 * which runs after LSP semantic tokens have been applied.
 */
class GoSemanticAnnotator : Annotator {

    companion object {
        private val KEY_REGEX = Regex("""([a-zA-Z_][a-zA-Z0-9_]*):["]""")
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is GoTag) return

        val text = element.text
        val keyRegex = KEY_REGEX

        keyRegex.findAll(text).forEach { match ->
            val keyGroup = match.groups[1] ?: return@forEach
            val start = element.textRange.startOffset + keyGroup.range.first
            val end   = element.textRange.startOffset + keyGroup.range.last + 1
            highlight(holder, TextRange(start, end), GoSyntaxHighlighter.STRUCT_TAG)
        }
    }

    private fun highlight(holder: AnnotationHolder, range: TextRange, key: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(key)
            .create()
    }
}
