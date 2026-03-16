package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.GoLexerAdapter
import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class GoSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val KEYWORD = createTextAttributesKey("GO_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val NUMBER = createTextAttributesKey("GO_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = createTextAttributesKey("GO_STRING", DefaultLanguageHighlighterColors.STRING)
        val COMMENT = createTextAttributesKey("GO_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT = createTextAttributesKey("GO_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val OPERATOR = createTextAttributesKey("GO_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val BRACES = createTextAttributesKey("GO_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BRACKETS = createTextAttributesKey("GO_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val PARENTHESES = createTextAttributesKey("GO_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BAD_CHARACTER = createTextAttributesKey("GO_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)
        // Add these inside your GoSyntaxHighlighter class or companion object

        val TYPE_NAME = createTextAttributesKey("GO_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
        val BUILTIN_TYPE = createTextAttributesKey("GO_BUILTIN_TYPE", DefaultLanguageHighlighterColors.CLASS_REFERENCE)
        val FUNCTION_DECLARATION = createTextAttributesKey("GO_FUNCTION_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val METHOD_DECLARATION = createTextAttributesKey("GO_METHOD_DECLARATION", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
        val FUNCTION_CALL = createTextAttributesKey("GO_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
        val METHOD_CALL   = createTextAttributesKey("GO_METHOD_CALL",   DefaultLanguageHighlighterColors.INSTANCE_METHOD)
        val STRUCT_FIELD = createTextAttributesKey("GO_STRUCT_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val STRUCT_TAG = createTextAttributesKey("GO_STRUCT_TAG", DefaultLanguageHighlighterColors.METADATA)

        val VALID_STRING_ESCAPE = createTextAttributesKey("GO_VALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
        val CONSTANT = createTextAttributesKey("GO_CONSTANT", DefaultLanguageHighlighterColors.STATIC_FIELD)

        // Semantic-token-only keys (only appear when LSP semantic tokens are enabled)
        val PACKAGE_NAME    = createTextAttributesKey("GO_PACKAGE_NAME",    DefaultLanguageHighlighterColors.CLASS_REFERENCE)
        val INTERFACE_TYPE  = createTextAttributesKey("GO_INTERFACE_TYPE",  DefaultLanguageHighlighterColors.INTERFACE_NAME)
        val PARAMETER       = createTextAttributesKey("GO_PARAMETER",       DefaultLanguageHighlighterColors.PARAMETER)
        val TYPE_PARAMETER  = createTextAttributesKey("GO_TYPE_PARAMETER",  DefaultLanguageHighlighterColors.IDENTIFIER)
        val NAMESPACE    = createTextAttributesKey("GO_NAMESPACE", DefaultLanguageHighlighterColors.IDENTIFIER)

        // Predeclared identifiers: nil, true, false
        val PREDECLARED     = createTextAttributesKey("GO_PREDECLARED",     DefaultLanguageHighlighterColors.KEYWORD)

        // Variable type modifier keys
        val VAR_FUNC        = createTextAttributesKey("GO_VAR_FUNC",        DefaultLanguageHighlighterColors.IDENTIFIER)
        val VAR_INTERFACE   = createTextAttributesKey("GO_VAR_INTERFACE",   DefaultLanguageHighlighterColors.INTERFACE_NAME)
        val VAR_POINTER     = createTextAttributesKey("GO_VAR_POINTER",     DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
        val VAR_STRING      = createTextAttributesKey("GO_VAR_STRING",      DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
        val VAR_NUMBER      = createTextAttributesKey("GO_VAR_NUMBER",      DefaultLanguageHighlighterColors.LOCAL_VARIABLE)

        // Parameter type modifier keys
        val PARAM_INTERFACE = createTextAttributesKey("GO_PARAM_INTERFACE", DefaultLanguageHighlighterColors.PARAMETER)
        val PARAM_STRUCT    = createTextAttributesKey("GO_PARAM_STRUCT",    DefaultLanguageHighlighterColors.PARAMETER)

        // Type with struct modifier — same visual default as interface type
        val STRUCT_TYPE     = createTextAttributesKey("GO_STRUCT_TYPE",     DefaultLanguageHighlighterColors.INTERFACE_NAME)

        // Format verbs inside strings (%s, %d, etc.)
        val FORMAT_VERB     = createTextAttributesKey("GO_FORMAT_VERB",     DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val STRING_KEYS = arrayOf(STRING)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val BLOCK_COMMENT_KEYS = arrayOf(BLOCK_COMMENT)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val PARENTHESES_KEYS = arrayOf(PARENTHESES)
        private val BAD_CHARACTER_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer {
        return GoLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            GoTypes.PACKAGE_KW, GoTypes.IMPORT_KW, GoTypes.FUNC_KW, GoTypes.TYPE_KW,
            GoTypes.VAR_KW, GoTypes.CONST_KW, GoTypes.STRUCT_KW, GoTypes.INTERFACE_KW,
            GoTypes.MAP_KW, GoTypes.CHAN_KW, GoTypes.GO_KW, GoTypes.DEFER_KW,
            GoTypes.RETURN_KW, GoTypes.BREAK_KW, GoTypes.CONTINUE_KW, GoTypes.GOTO_KW,
            GoTypes.FALLTHROUGH_KW, GoTypes.IF_KW, GoTypes.ELSE_KW, GoTypes.SWITCH_KW,
            GoTypes.CASE_KW, GoTypes.DEFAULT_KW, GoTypes.SELECT_KW, GoTypes.FOR_KW,
            GoTypes.RANGE_KW -> KEYWORD_KEYS

            GoTypes.INT_LIT, GoTypes.FLOAT_LIT, GoTypes.IMAGINARY_LIT -> NUMBER_KEYS
            GoTypes.STRING_LIT, GoTypes.RUNE_LIT -> STRING_KEYS
            GoTypes.LINE_COMMENT -> COMMENT_KEYS
            GoTypes.BLOCK_COMMENT -> BLOCK_COMMENT_KEYS

            GoTypes.LBRACE, GoTypes.RBRACE -> BRACES_KEYS
            GoTypes.LBRACK, GoTypes.RBRACK -> BRACKETS_KEYS
            GoTypes.LPAREN, GoTypes.RPAREN -> PARENTHESES_KEYS

            GoTypes.ELLIPSIS, GoTypes.EQEQ, GoTypes.NEQ, GoTypes.LESS_EQ, GoTypes.GREATER_EQ,
            GoTypes.ASSIGN, GoTypes.COND_OR, GoTypes.COND_AND, GoTypes.SEND_CHANNEL,
            GoTypes.PLUS_PLUS, GoTypes.MINUS_MINUS, GoTypes.SHIFT_LEFT, GoTypes.SHIFT_RIGHT,
            GoTypes.BIT_CLEAR, GoTypes.COLON, GoTypes.SEMICOLON, GoTypes.COMMA,
            GoTypes.DOT, GoTypes.EQ, GoTypes.LESS, GoTypes.GREATER, GoTypes.PLUS,
            GoTypes.MINUS, GoTypes.BIT_OR, GoTypes.BIT_XOR, GoTypes.MUL, GoTypes.QUOTIENT,
            GoTypes.REMAINDER, GoTypes.BIT_AND, GoTypes.NOT, GoTypes.TILDE -> OPERATOR_KEYS

            TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS
            else -> EMPTY_KEYS
        }
    }
}
