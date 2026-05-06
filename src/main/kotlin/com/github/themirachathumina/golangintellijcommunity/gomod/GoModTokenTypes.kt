package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.psi.tree.IElementType

/*
 * IElementType constants for all tokens produced by GoModLexer.
 */
object GoModTokenTypes {
    @JvmField val KEYWORD     = IElementType("GOMOD_KEYWORD",      GoModLanguage)
    @JvmField val IDENTIFIER  = IElementType("GOMOD_IDENTIFIER",   GoModLanguage)
    @JvmField val MODULE_PATH = IElementType("GOMOD_MODULE_PATH",  GoModLanguage)
    @JvmField val VERSION     = IElementType("GOMOD_VERSION",      GoModLanguage)
    @JvmField val STRING      = IElementType("GOMOD_STRING",       GoModLanguage)
    @JvmField val COMMENT     = IElementType("GOMOD_COMMENT",      GoModLanguage)
    @JvmField val ARROW       = IElementType("GOMOD_ARROW",        GoModLanguage)
    @JvmField val LPAREN      = IElementType("GOMOD_LPAREN",       GoModLanguage)
    @JvmField val RPAREN      = IElementType("GOMOD_RPAREN",       GoModLanguage)
}
