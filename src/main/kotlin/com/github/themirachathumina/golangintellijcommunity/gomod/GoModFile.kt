package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class GoModFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, GoModLanguage) {
    override fun getFileType(): FileType = GoModFileType
}
