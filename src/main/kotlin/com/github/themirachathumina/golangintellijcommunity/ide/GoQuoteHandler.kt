package com.github.themirachathumina.golangintellijcommunity.ide

import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes
import com.intellij.codeInsight.editorActions.QuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator

class GoQuoteHandler : QuoteHandler {
    override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType
        if (tokenType == GoTypes.STRING_LIT || tokenType == GoTypes.RUNE_LIT) {
            return iterator.start == offset
        }
        return false
    }

    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType
        if (tokenType == GoTypes.STRING_LIT || tokenType == GoTypes.RUNE_LIT) {
            val start = iterator.start
            val end = iterator.end
            return end - start >= 1 && offset == end - 1
        }
        return false
    }

    override fun hasNonClosedLiteral(editor: Editor, iterator: HighlighterIterator, offset: Int): Boolean {
        return true
    }

    override fun isInsideLiteral(iterator: HighlighterIterator): Boolean {
        return false
    }
}
