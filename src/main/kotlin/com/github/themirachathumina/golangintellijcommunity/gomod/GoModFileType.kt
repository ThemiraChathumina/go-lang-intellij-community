package com.github.themirachathumina.golangintellijcommunity.gomod

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object GoModFileType : LanguageFileType(GoModLanguage) {
    override fun getName(): String = "Go Module File"
    override fun getDescription(): String = "Go module and workspace files (go.mod, go.work)"
    override fun getDefaultExtension(): String = ""
    override fun getIcon(): Icon = GoIcons.FILE
}
