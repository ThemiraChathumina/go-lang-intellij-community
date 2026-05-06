package com.github.themirachathumina.golangintellijcommunity

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

/*
 * Custom IElementType for Go lexer tokens, bound to GoLanguage.
 * toString() prefixes the debug name with "GoTokenType." for easier debugging.
 */
class GoTokenType(@NonNls debugName: String) : IElementType(debugName, GoLanguage) {
    override fun toString(): String {
        return "GoTokenType." + super.toString()
    }
}
