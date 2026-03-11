package com.github.themirachathumina.golangintellijcommunity

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class GoFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, GoLanguage) {

    override fun getFileType(): FileType {
        return GoFileType
    }

    override fun toString(): String {
        return "Go File"
    }
}