package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

/*
 * Generates test stubs for all non-test functions in the current Go source file.
 * Available from both the editor right-click menu and the project tree.
 * Disabled for _test.go files.
 */
class GoGenerateTestsForFileAction : AnAction("Generate Tests for File") {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = vFile != null
            && vFile.extension == GoConstants.FileExtensions.GO_EXT
            && !vFile.name.endsWith(GoConstants.FileExtensions.TEST_SUFFIX)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        GoTestsGenerator.generateForFile(e.project ?: return, vFile)
    }
}
