package com.github.themirachathumina.golangintellijcommunity.ide

import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

/*
 * Paired brace matcher for Go, enabling automatic matching and highlighting of {/}, [/], and (/).
 * Structural braces ({/}) use the 'structural' flag.
 */
class GoPairedBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = arrayOf(
        BracePair(GoTypes.LBRACE, GoTypes.RBRACE, true),
        BracePair(GoTypes.LBRACK, GoTypes.RBRACK, false),
        BracePair(GoTypes.LPAREN, GoTypes.RPAREN, false)
    )

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset
}
