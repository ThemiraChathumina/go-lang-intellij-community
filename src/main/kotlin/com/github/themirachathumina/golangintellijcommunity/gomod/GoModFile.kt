package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

/*
 * PSI file node for go.mod, go.work, and go.sum files. Returns GoModFileType as its file type.
 */
class GoModFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, GoModLanguage) {
    override fun getFileType(): FileType = GoModFileType
}
