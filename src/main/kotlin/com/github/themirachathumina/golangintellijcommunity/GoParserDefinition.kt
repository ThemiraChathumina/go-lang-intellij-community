package com.github.themirachathumina.golangintellijcommunity

import com.github.themirachathumina.golangintellijcommunity.parser.GoParser
import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/*
 * Wires together all language-infrastructure pieces for Go: the lexer, parser,
 * comment/string token sets, and the PSI file factory.
 */
class GoParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(GoLanguage)

        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)

        val COMMENTS = TokenSet.create(GoTypes.LINE_COMMENT, GoTypes.BLOCK_COMMENT)

        val STRINGS = TokenSet.create(GoTypes.STRING_LIT, GoTypes.RUNE_LIT)
    }

    override fun createLexer(project: Project?): Lexer {
        return GoLexerAdapter()
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITE_SPACES
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return STRINGS
    }

    override fun createParser(project: Project?): PsiParser {
        return GoParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return GoFile(viewProvider)
    }

    override fun createElement(node: ASTNode?): PsiElement {
        return GoTypes.Factory.createElement(node)
    }
}
