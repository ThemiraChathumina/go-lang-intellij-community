package com.github.themirachathumina.golangintellijcommunity

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object GoFileType : LanguageFileType(GoLanguage) {
    override fun getName(): String = "Go File"

    override fun getDescription(): String = "Go language file"

    override fun getDefaultExtension(): String = "go"

    override fun getIcon(): Icon = GoIcons.FILE
}