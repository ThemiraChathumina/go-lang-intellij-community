package com.github.themirachathumina.golangintellijcommunity.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiFile
import com.redhat.devtools.lsp4ij.features.semanticTokens.SemanticTokensColorsProvider

/**
 * Maps gopls semantic tokens to IntelliJ colors.
 * This is the sole source of semantic highlighting — all semantic coloring
 * is driven by LSP tokens, not PSI analysis.
 *
 * Token types handled:
 *   namespace                              → null               (delegated to GoSemanticAnnotator: imports→STRING, code qualifiers→PACKAGE_NAME)
 *   variable [readonly, defaultLibrary]    → PREDECLARED        (nil, true, false)
 *   variable [readonly]                    → CONSTANT           (Go const declarations and usages)
 *   variable [signature]                   → VAR_FUNC           (function-valued variable)
 *   variable [interface]                   → VAR_INTERFACE      (interface-typed variable)
 *   variable [pointer]                     → VAR_POINTER        (pointer-typed variable)
 *   variable [string]                      → VAR_STRING         (string-typed variable)
 *   variable [number]                      → VAR_NUMBER         (numeric-typed variable)
 *   function [definition]                  → FUNCTION_DECLARATION
 *   function (no definition)               → FUNCTION_CALL
 *   method [definition]                    → METHOD_DECLARATION
 *   method (no definition)                 → METHOD_CALL
 *   type [defaultLibrary]                  → BUILTIN_TYPE       (string, int, bool, error, etc.)
 *   type [interface]                       → INTERFACE_TYPE     (interface type names)
 *   type [struct]                          → STRUCT_TYPE        (struct type names)
 *   type (other)                           → TYPE_NAME          (other named types)
 *   property                               → STRUCT_FIELD       (struct field access and declarations)
 *   parameter [interface]                  → PARAM_INTERFACE    (interface-typed parameter)
 *   parameter [struct]                     → PARAM_STRUCT       (struct-typed parameter)
 *   parameter (other)                      → PARAMETER          (function/method parameters)
 *   typeParameter                          → TYPE_PARAMETER     (generic type parameters)
 *   string [format]                        → FORMAT_VERB        (format verbs like %s, %d)
 *
 * Returning null delegates to the lexer-based syntax highlighter,
 * which covers keywords, operators, strings, numbers, etc.
 */
class GoSemanticTokensColorsProvider : SemanticTokensColorsProvider {

    override fun getTextAttributesKey(
        tokenType: String,
        tokenModifiers: List<String>,
        file: PsiFile
    ): TextAttributesKey? = when (tokenType) {

        // Namespace coloring is delegated to GoSemanticAnnotator which has PSI context
        // to distinguish import declarations (→ STRING) from code qualifiers (→ PACKAGE_NAME).
        // Semantic token highlights layer above annotators, so returning null here is required.
        "namespace" -> GoSyntaxHighlighter.PACKAGE_NAME

        "variable" -> when {
            "readonly" in tokenModifiers && "defaultLibrary" in tokenModifiers -> GoSyntaxHighlighter.BUILTIN_TYPE
            "readonly" in tokenModifiers -> GoSyntaxHighlighter.CONSTANT
            "signature" in tokenModifiers -> GoSyntaxHighlighter.VAR_FUNC
            "interface" in tokenModifiers -> GoSyntaxHighlighter.TYPE_NAME
            "pointer" in tokenModifiers   -> GoSyntaxHighlighter.VAR_POINTER
            "string" in tokenModifiers    -> GoSyntaxHighlighter.VAR_STRING
            "number" in tokenModifiers    -> GoSyntaxHighlighter.VAR_NUMBER
            else -> null
        }

        "function" -> if ("definition" in tokenModifiers)
            GoSyntaxHighlighter.FUNCTION_DECLARATION
        else
            GoSyntaxHighlighter.FUNCTION_CALL

        "method" -> if ("definition" in tokenModifiers)
            GoSyntaxHighlighter.METHOD_DECLARATION
        else
            GoSyntaxHighlighter.METHOD_CALL

        "type" -> when {
            "defaultLibrary" in tokenModifiers -> GoSyntaxHighlighter.BUILTIN_TYPE
            "interface" in tokenModifiers      -> GoSyntaxHighlighter.INTERFACE_TYPE
            "struct" in tokenModifiers         -> GoSyntaxHighlighter.STRUCT_TYPE
            else                               -> GoSyntaxHighlighter.TYPE_NAME
        }

        "property" -> GoSyntaxHighlighter.STRUCT_FIELD

        "parameter" -> when {
            "interface" in tokenModifiers -> GoSyntaxHighlighter.PARAM_INTERFACE
            "struct" in tokenModifiers    -> GoSyntaxHighlighter.PARAM_STRUCT
            else                          -> GoSyntaxHighlighter.PARAMETER
        }

        "typeParameter" -> GoSyntaxHighlighter.TYPE_PARAMETER

        "string" -> if ("format" in tokenModifiers) GoSyntaxHighlighter.FORMAT_VERB else null

        else -> null
    }
}
