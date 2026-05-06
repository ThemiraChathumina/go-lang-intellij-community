package com.github.themirachathumina.golangintellijcommunity

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

/*
 * Custom IElementType for Go PSI node types, bound to GoLanguage.
 */
class GoElementType(@NonNls debugName: String) : IElementType(debugName, GoLanguage)