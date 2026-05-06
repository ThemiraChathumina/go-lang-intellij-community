package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.lang.Language

/*
 * Language singleton for Go module files (go.mod, go.work, go.sum).
 */
object GoModLanguage : Language("GoMod") {
    override fun getDisplayName(): String = "Go Module"
}
