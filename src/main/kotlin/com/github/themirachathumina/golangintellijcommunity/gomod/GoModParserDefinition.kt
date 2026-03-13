package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

val GOMOD_FILE_ELEMENT_TYPE = IFileElementType(GoModLanguage)

/**
 * Minimal parser definition for GoMod language.
 * We don't need a real parser — gopls handles all language intelligence.
 * This exists so IntelliJ creates a GoModFile (not a PlainTextFile) whose
 * getLanguage() returns GoModLanguage (ID "GoMod"), preventing lsp4ij from
 * treating go.mod/go.work/go.sum as plain-text and showing the
 * "install language server" banner.
 */
class GoModParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = GoModLexer()

    override fun createParser(project: Project): PsiParser =
        PsiParser { _, builder ->
            val root = builder.mark()
            while (!builder.eof()) builder.advanceLexer()
            root.done(GOMOD_FILE_ELEMENT_TYPE)
            builder.treeBuilt
        }

    override fun getFileNodeType(): IFileElementType = GOMOD_FILE_ELEMENT_TYPE

    override fun getCommentTokens(): TokenSet = TokenSet.create(GoModTokenTypes.COMMENT)

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(GoModTokenTypes.STRING)

    override fun createElement(node: ASTNode): PsiElement =
        throw UnsupportedOperationException("GoMod PSI elements are not used directly")

    override fun createFile(viewProvider: FileViewProvider): PsiFile = GoModFile(viewProvider)
}
