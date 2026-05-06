package com.github.themirachathumina.golangintellijcommunity.highlight

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

/*
 * PSI mixin for Go string literals that enables language injection (e.g. RegExp inside backtick strings).
 * Implements PsiLanguageInjectionHost so IntelliJ can embed other language fragments into Go strings.
 */
abstract class GoStringLiteralMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost {

    // Tells the IDE this string is allowed to accept language injections
    override fun isValidHost(): Boolean = true

    // Required boilerplate for read-only injections
    override fun updateText(text: String): PsiLanguageInjectionHost {
        return this
    }

    // Tells the IDE how to handle string escapes (like \n) inside the injection
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {
        return LiteralTextEscaper.createSimple(this)
    }
}