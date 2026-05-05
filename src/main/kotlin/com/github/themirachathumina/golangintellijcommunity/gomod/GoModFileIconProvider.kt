package com.github.themirachathumina.golangintellijcommunity.gomod

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.ide.FileIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class GoModFileIconProvider : FileIconProvider {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? = when (file.name) {
        GoConstants.FileExtensions.MOD  -> GoIcons.MOD_FILE
        GoConstants.FileExtensions.WORK -> GoIcons.WORK_FILE
        GoConstants.FileExtensions.SUM  -> GoIcons.SUM_FILE
        else                            -> null
    }
}
