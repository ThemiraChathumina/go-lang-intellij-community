package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/**
 * Hand-written lexer for go.mod and go.work files.
 *
 * Tokens:
 *   - Keywords: module, go, require, replace, exclude, retract, use, toolchain
 *   - Version literals: v1.2.3, v0.0.0-20230101-abcdef (start with 'v' + digit)
 *   - Strings: "..."
 *   - Comments: // ...
 *   - Arrow: =>
 *   - Parens: ( )
 *   - Identifiers: module paths and anything else word-like
 *   - Whitespace, bad character
 */
class GoModLexer : LexerBase() {

    private var buffer: CharSequence = ""
    private var bufferEnd = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.bufferEnd = endOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        advance()
    }

    override fun getState(): Int = 0
    override fun getTokenType(): IElementType? = tokenType
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = bufferEnd

    override fun advance() {
        tokenStart = tokenEnd
        if (tokenStart >= bufferEnd) {
            tokenType = null
            return
        }

        val c = buffer[tokenStart]

        when {
            // Line comment
            c == '/' && peek(1) == '/' -> {
                tokenEnd = tokenStart + 2
                while (tokenEnd < bufferEnd && buffer[tokenEnd] != '\n') tokenEnd++
                tokenType = GoModTokenTypes.COMMENT
            }

            // String literal
            c == '"' -> {
                tokenEnd = tokenStart + 1
                while (tokenEnd < bufferEnd && buffer[tokenEnd] != '"') {
                    if (buffer[tokenEnd] == '\\') tokenEnd++ // skip escaped char
                    tokenEnd++
                }
                if (tokenEnd < bufferEnd) tokenEnd++ // consume closing quote
                tokenType = GoModTokenTypes.STRING
            }

            // Arrow operator =>
            c == '=' && peek(1) == '>' -> {
                tokenEnd = tokenStart + 2
                tokenType = GoModTokenTypes.ARROW
            }

            c == '(' -> { tokenEnd = tokenStart + 1; tokenType = GoModTokenTypes.LPAREN }
            c == ')' -> { tokenEnd = tokenStart + 1; tokenType = GoModTokenTypes.RPAREN }

            // Whitespace
            c.isWhitespace() -> {
                tokenEnd = tokenStart + 1
                while (tokenEnd < bufferEnd && buffer[tokenEnd].isWhitespace()) tokenEnd++
                tokenType = TokenType.WHITE_SPACE
            }

            // Bare version number: starts with a digit (e.g. 1.21, 1.21.0)
            c.isDigit() -> {
                tokenEnd = tokenStart + 1
                while (tokenEnd < bufferEnd && (buffer[tokenEnd].isDigit() || buffer[tokenEnd] == '.')) tokenEnd++
                tokenType = GoModTokenTypes.VERSION
            }

            // Version literal: starts with 'v' followed by a digit (e.g. v1.2.3)
            c == 'v' && tokenStart + 1 < bufferEnd && buffer[tokenStart + 1].isDigit() -> {
                tokenEnd = tokenStart + 1
                while (tokenEnd < bufferEnd && isVersionChar(buffer[tokenEnd])) tokenEnd++
                tokenType = GoModTokenTypes.VERSION
            }

            // Keyword, module path, or identifier
            isIdentStart(c) -> {
                tokenEnd = tokenStart + 1
                while (tokenEnd < bufferEnd && isIdentContinue(buffer[tokenEnd])) tokenEnd++
                val word = buffer.substring(tokenStart, tokenEnd)
                tokenType = when {
                    KEYWORDS.contains(word) -> GoModTokenTypes.KEYWORD
                    word.contains('/')      -> GoModTokenTypes.MODULE_PATH
                    else                   -> GoModTokenTypes.IDENTIFIER
                }
            }

            else -> {
                tokenEnd = tokenStart + 1
                tokenType = TokenType.BAD_CHARACTER
            }
        }
    }

    private fun peek(offset: Int): Char {
        val idx = tokenStart + offset
        return if (idx < bufferEnd) buffer[idx] else '\u0000'
    }

    private fun isIdentStart(c: Char) =
        c.isLetter() || c == '_' || c == '.' || c == '-' || c == '~'

    // Module paths can contain letters, digits, _, -, ., /, @, +, ~
    private fun isIdentContinue(c: Char) =
        c.isLetterOrDigit() || c in "_-./@+~"

    // Version chars: alphanumeric, ., -, +
    private fun isVersionChar(c: Char) =
        c.isLetterOrDigit() || c == '.' || c == '-' || c == '+'

    companion object {
        private val KEYWORDS = setOf(
            "module", "go", "require", "replace", "exclude", "retract", "use", "tool", "toolchain"
        )
    }
}
