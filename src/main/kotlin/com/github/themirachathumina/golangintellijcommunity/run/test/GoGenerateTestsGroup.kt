package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.psi.PsiDirectory

/*
 * Submenu group for test generation actions. Disappears from the context menu
 * when neither a Go source file nor a directory with Go sources is in context.
 */
class GoGenerateTestsGroup : DefaultActionGroup("Go Generate", true) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val hasGoSourceFile = vFile != null
            && vFile.extension == GoConstants.FileExtensions.GO_EXT
            && !vFile.name.endsWith(GoConstants.FileExtensions.TEST_SUFFIX)

        val dir = e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory
        val hasGoDirectory = dir != null
            && dir.files.any { it is GoFile && !it.name.endsWith(GoConstants.FileExtensions.TEST_SUFFIX) }

        e.presentation.isEnabledAndVisible = hasGoSourceFile || hasGoDirectory
    }
}
