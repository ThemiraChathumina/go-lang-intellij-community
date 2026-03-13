package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.lexer.EmptyLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

class GoModSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        if (virtualFile?.name == "go.sum") NoOpSyntaxHighlighter else GoModSyntaxHighlighter()
}

private object NoOpSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = EmptyLexer()
    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = emptyArray()
}
