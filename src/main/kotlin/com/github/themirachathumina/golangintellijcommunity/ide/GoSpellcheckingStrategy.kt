package com.github.themirachathumina.golangintellijcommunity.ide

import com.github.themirachathumina.golangintellijcommunity.psi.GoTypes
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer
import com.intellij.psi.util.elementType

/*
 * Enables spell-checking in Go comments and string/rune literals.
 * All other token types (identifiers, keywords, etc.) are excluded from spell checking.
 */
class GoSpellcheckingStrategy : SpellcheckingStrategy() {
    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        val type = element.elementType
        if (element is PsiComment || type == GoTypes.STRING_LIT || type == GoTypes.RUNE_LIT) {
            return TEXT_TOKENIZER
        }
        return EMPTY_TOKENIZER
    }
}
