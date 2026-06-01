package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDirectory

/*
 * Generates test stubs for all non-test functions in every Go source file
 * within the selected package directory. Available only from the project tree.
 */
class GoGenerateTestsForPackageAction : AnAction("Generate Tests for Package") {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val dir = e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory
        e.presentation.isEnabledAndVisible = dir != null && hasSourceFiles(dir)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val dir = e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory ?: return
        GoTestsGenerator.generateForDirectory(e.project ?: return, dir.virtualFile)
    }

    private fun hasSourceFiles(dir: PsiDirectory): Boolean =
        dir.files.any { it is GoFile && !it.name.endsWith(GoConstants.FileExtensions.TEST_SUFFIX) }
}
