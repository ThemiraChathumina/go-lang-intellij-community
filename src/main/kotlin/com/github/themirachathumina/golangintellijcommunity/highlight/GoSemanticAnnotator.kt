package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.psi.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class GoSemanticAnnotator : Annotator {

    private val builtInTypes = setOf("string", "int", "int8", "int16", "int32", "int64",
        "uint", "uint8", "uint16", "uint32", "uint64", "float32", "float64",
        "bool", "error", "byte", "rune", "complex64", "complex128", "any")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            // 1. Function & Method Declarations
            is GoFunctionName -> applyStyle(holder, element, GoSyntaxHighlighter.FUNCTION_DECLARATION)
            is GoMethodName -> applyStyle(holder, element, GoSyntaxHighlighter.METHOD_DECLARATION)

            // 2. Type Definitions (Structs, Interfaces, Aliases)
            // FIX: We specifically extract ONLY the identifier leaf node.
            // This prevents the annotator from accidentally coloring the entire { } struct body!
            is GoTypeDef, is GoAliasDecl -> {
                val identifier = element.node.findChildByType(GoTypes.IDENTIFIER)?.psi
                if (identifier != null) {
                    applyStyle(holder, identifier, GoSyntaxHighlighter.TYPE_NAME)
                }
            }

            // 3. Type Usages & Built-ins
            is GoTypeName -> {
                val isBuiltin = builtInTypes.contains(element.text)
                val style = if (isBuiltin) GoSyntaxHighlighter.BUILTIN_TYPE else GoSyntaxHighlighter.TYPE_NAME
                applyStyle(holder, element, style)
            }

            // 4. Struct Field Declarations
            is GoFieldDecl -> {
                val identifiers = element.identifierList?.node?.getChildren(TokenSet.create(GoTypes.IDENTIFIER))
                identifiers?.forEach { idNode ->
                    applyStyle(holder, idNode.psi, GoSyntaxHighlighter.STRUCT_FIELD)
                }
            }

            // 5. Struct Tags
            is GoTag -> {
                val text = element.text
                // FIX: We only want to highlight the KEYS (like 'json' or 'xml').
                // We leave the quotes and values alone so they retain their standard green string color!
                val keyRegex = Regex("""([a-zA-Z_][a-zA-Z0-9_]*):\"""")

                keyRegex.findAll(text).forEach { match ->
                    val keyGroup = match.groups[1]
                    if (keyGroup != null) {
                        val start = element.textRange.startOffset + keyGroup.range.first
                        val end = element.textRange.startOffset + keyGroup.range.last + 1

                        applyStyle(holder, TextRange(start, end), GoSyntaxHighlighter.STRUCT_TAG)
                    }
                }
            }

            // 6. Selectors (Method Calls vs Field Access)
            is GoSelector -> {
                val currentSuffix = element.parent as? GoPrimarySuffix
                val primaryExpr = currentSuffix?.parent as? GoPrimaryExpr
                val suffixList = primaryExpr?.primarySuffixList ?: emptyList()
                val currentIndex = suffixList.indexOf(currentSuffix)

                val isMethodCall = if (currentIndex != -1 && currentIndex + 1 < suffixList.size) {
                    suffixList[currentIndex + 1].arguments != null
                } else false

                val style = if (isMethodCall) GoSyntaxHighlighter.METHOD_CALL else GoSyntaxHighlighter.STRUCT_FIELD
                val identifier = element.node.findChildByType(GoTypes.IDENTIFIER)

                if (identifier != null) {
                    applyStyle(holder, identifier.psi, style)
                }
            }

            // 7. Function Calls
            is GoOperandName -> {
                val primaryExpr = element.parent?.parent?.parent as? GoPrimaryExpr
                if (primaryExpr != null) {
                    val hasArgs = primaryExpr.primarySuffixList.any { it.arguments != null }
                    if (hasArgs) {
                        applyStyle(holder, element, GoSyntaxHighlighter.FUNCTION_CALL)
                    }
                }
            }
        }
    }

    // Helper: Apply style to a full PsiElement
    private fun applyStyle(holder: AnnotationHolder, element: PsiElement, attr: TextAttributesKey) {
        applyStyle(holder, element.textRange, attr)
    }

    // Helper: Apply style to a specific mathematical text range (Used for our Struct Tags)
    private fun applyStyle(holder: AnnotationHolder, range: TextRange, attr: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(attr)
            .create()
    }
}