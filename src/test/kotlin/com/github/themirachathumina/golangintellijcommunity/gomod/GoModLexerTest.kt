package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.psi.TokenType
import org.junit.Assert.assertEquals
import org.junit.Test

class GoModLexerTest {

    @Test
    fun testKeywords() {
        val lexer = GoModLexer()
        lexer.start("module go require replace exclude retract use tool toolchain")
        
        val expected = listOf(
            GoModTokenTypes.KEYWORD, // module
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD, // go
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD, // require
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD, // replace
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD, // exclude
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD, // retract
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD, // use
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD, // tool
            TokenType.WHITE_SPACE,
            GoModTokenTypes.KEYWORD  // toolchain
        )
        
        val actual = mutableListOf<com.intellij.psi.tree.IElementType?>()
        while (lexer.tokenType != null) {
            actual.add(lexer.tokenType)
            lexer.advance()
        }
        
        assertEquals(expected, actual)
    }

    @Test
    fun testVersions() {
        val lexer = GoModLexer()
        lexer.start("1.21 v1.2.3 v0.0.0-20230101-abcdef")
        
        val expected = listOf(
            GoModTokenTypes.VERSION,
            TokenType.WHITE_SPACE,
            GoModTokenTypes.VERSION,
            TokenType.WHITE_SPACE,
            GoModTokenTypes.VERSION
        )
        
        val actual = mutableListOf<com.intellij.psi.tree.IElementType?>()
        while (lexer.tokenType != null) {
            actual.add(lexer.tokenType)
            lexer.advance()
        }
        
        assertEquals(expected, actual)
    }

    @Test
    fun testCommentsAndStrings() {
        val lexer = GoModLexer()
        lexer.start("// this is a comment\n\"this is a string\"")
        
        val expected = listOf(
            GoModTokenTypes.COMMENT,
            TokenType.WHITE_SPACE, // \n
            GoModTokenTypes.STRING
        )
        
        val actual = mutableListOf<com.intellij.psi.tree.IElementType?>()
        while (lexer.tokenType != null) {
            actual.add(lexer.tokenType)
            lexer.advance()
        }
        
        assertEquals(expected, actual)
    }

    @Test
    fun testOperatorsAndParens() {
        val lexer = GoModLexer()
        lexer.start("=> ( )")
        
        val expected = listOf(
            GoModTokenTypes.ARROW,
            TokenType.WHITE_SPACE,
            GoModTokenTypes.LPAREN,
            TokenType.WHITE_SPACE,
            GoModTokenTypes.RPAREN
        )
        
        val actual = mutableListOf<com.intellij.psi.tree.IElementType?>()
        while (lexer.tokenType != null) {
            actual.add(lexer.tokenType)
            lexer.advance()
        }
        
        assertEquals(expected, actual)
    }

    @Test
    fun testModulePathAndIdentifier() {
        val lexer = GoModLexer()
        lexer.start("github.com/user/repo simple_id")
        
        val expected = listOf(
            GoModTokenTypes.MODULE_PATH,
            TokenType.WHITE_SPACE,
            GoModTokenTypes.IDENTIFIER
        )
        
        val actual = mutableListOf<com.intellij.psi.tree.IElementType?>()
        while (lexer.tokenType != null) {
            actual.add(lexer.tokenType)
            lexer.advance()
        }
        
        assertEquals(expected, actual)
    }
}
