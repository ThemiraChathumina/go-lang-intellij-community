package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/*
 * Token-based syntax highlighter for go.mod and go.work files. Maps GoModTokenTypes
 * to editor color attributes for keywords, strings, comments, versions, operators, and parentheses.
 */
class GoModSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val KEYWORD  = createTextAttributesKey("GOMOD_KEYWORD",  DefaultLanguageHighlighterColors.KEYWORD)
        val STRING   = createTextAttributesKey("GOMOD_STRING",   DefaultLanguageHighlighterColors.STRING)
        val COMMENT  = createTextAttributesKey("GOMOD_COMMENT",  DefaultLanguageHighlighterColors.LINE_COMMENT)
        val VERSION  = createTextAttributesKey("GOMOD_VERSION",  DefaultLanguageHighlighterColors.NUMBER)
        val OPERATOR = createTextAttributesKey("GOMOD_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PARENS   = createTextAttributesKey("GOMOD_PARENS",   DefaultLanguageHighlighterColors.PARENTHESES)

        private val KEYWORD_KEYS  = arrayOf(KEYWORD)
        private val STRING_KEYS   = arrayOf(STRING)
        private val COMMENT_KEYS  = arrayOf(COMMENT)
        private val VERSION_KEYS  = arrayOf(VERSION)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val PARENS_KEYS   = arrayOf(PARENS)
        private val EMPTY_KEYS    = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = GoModLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        GoModTokenTypes.KEYWORD      -> KEYWORD_KEYS
        GoModTokenTypes.MODULE_PATH  -> STRING_KEYS
        GoModTokenTypes.STRING       -> STRING_KEYS
        GoModTokenTypes.COMMENT      -> COMMENT_KEYS
        GoModTokenTypes.VERSION      -> VERSION_KEYS
        GoModTokenTypes.ARROW        -> OPERATOR_KEYS
        GoModTokenTypes.LPAREN,
        GoModTokenTypes.RPAREN       -> PARENS_KEYS
        TokenType.BAD_CHARACTER      -> EMPTY_KEYS
        else                         -> EMPTY_KEYS
    }
}
