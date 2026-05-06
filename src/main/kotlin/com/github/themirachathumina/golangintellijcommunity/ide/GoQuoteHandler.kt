package com.github.themirachathumina.golangintellijcommunity.ide

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.codeInsight.editorActions.QuoteHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes

/*
 * Quote handler for Go string and rune literals. Tells the IDE which tokens represent
 * opening and closing quotes so it can auto-close them when typing.
 */
class GoQuoteHandler : QuoteHandler {
    private val LOG = Logger.getInstance(GoQuoteHandler::class.java)

    override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType
        val result = tokenType == GoTypes.STRING_LIT || tokenType == GoTypes.RUNE_LIT
        if (result) {
            LOG.debug("${GoConstants.Log.PREFIX} Detected opening quote for token type: $tokenType")
        }
        return result
    }

    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType
        val result = tokenType == GoTypes.STRING_LIT || tokenType == GoTypes.RUNE_LIT
        if (result) {
            LOG.debug("${GoConstants.Log.PREFIX} Detected closing quote for token type: $tokenType")
        }
        return result
    }

    override fun hasNonClosedLiteral(editor: Editor, iterator: HighlighterIterator, offset: Int): Boolean = true

    override fun isInsideLiteral(iterator: HighlighterIterator): Boolean = false
}
