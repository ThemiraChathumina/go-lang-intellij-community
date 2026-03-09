package com.github.themirachathumina.golangintellijcommunity

import com.github.themirachathumina.golangintellijcommunity.GoLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class GoElementType(@NonNls debugName: String) : IElementType(debugName, GoLanguage)