package com.github.themirachathumina.golangintellijcommunity

import com.github.themirachathumina.golangintellijcommunity.GoLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class GoTokenType(@NonNls debugName: String) : IElementType(debugName, GoLanguage) {
    override fun toString(): String {
        return "GoTokenType." + super.toString()
    }
}