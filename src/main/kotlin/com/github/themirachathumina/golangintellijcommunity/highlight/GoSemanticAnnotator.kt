package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.psi.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * PSI-based annotator covering two concerns:
 *
 * 1. Namespace coloring — the SemanticTokensColorsProvider returns null for `namespace`
 *    tokens because it has no positional context. This annotator uses the PSI tree to
 *    distinguish:
 *      - Import declaration aliases (e.g. `f` in `import f "fmt"`) → STRING color
 *      - Package qualifiers in code (e.g. `fmt` in `fmt.Println()`)  → PACKAGE_NAME color
 *    Regular import paths (`"fmt"`) are already STRING-colored by the lexer.
 *
 * 2. Struct tag key highlighting — regex-based, no LSP semantic token equivalent.
 */
class GoSemanticAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            // Import path string literals — force STRING color so any semantic token
            // (e.g. namespace PACKAGE_NAME) that overlaps the import path is overridden.
            is GoImportPath -> highlight(holder, element.stringLit, GoSyntaxHighlighter.STRING)

            // Struct tag keys (e.g. `json` in `\`json:"name"\``) — regex-based, no LSP equivalent
            is GoTag -> {
                val text = element.text
                val keyRegex = Regex("""([a-zA-Z_][a-zA-Z0-9_]*):["]""")

                keyRegex.findAll(text).forEach { match ->
                    val keyGroup = match.groups[1] ?: return@forEach
                    val start = element.textRange.startOffset + keyGroup.range.first
                    val end   = element.textRange.startOffset + keyGroup.range.last + 1
                    highlight(holder, TextRange(start, end), GoSyntaxHighlighter.STRUCT_TAG)
                }
            }
        }
    }

    private fun highlight(holder: AnnotationHolder, element: PsiElement, key: com.intellij.openapi.editor.colors.TextAttributesKey) {
        highlight(holder, element.textRange, key)
    }

    private fun highlight(holder: AnnotationHolder, range: TextRange, key: com.intellij.openapi.editor.colors.TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(key)
            .create()
    }
}
