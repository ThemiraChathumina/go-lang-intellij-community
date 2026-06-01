package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiTreeUtil

/*
 * Generates a test stub for the Go function currently under the editor caret.
 * Disabled for main, and for functions that are already tests/benchmarks/fuzz targets/examples.
 */
class GoGenerateTestsForFunctionAction : AnAction("Generate Tests for Function") {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = resolveTarget(e) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val (funcName, sourceFile) = resolveTarget(e) ?: return
        GoTestsGenerator.generateForFunction(e.project ?: return, funcName, sourceFile)
    }

    private fun resolveTarget(e: AnActionEvent): Pair<String, VirtualFile>? {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) as? GoFile ?: return null
        if (psiFile.name.endsWith("_test.go")) return null

        val element = psiFile.findElementAt(editor.caretModel.offset) ?: return null
        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java) ?: return null
        val funcName = function.functionName.identifier.text ?: return null

        if (funcName == "main") return null
        if (funcName.startsWith("Test") || funcName.startsWith("Benchmark") ||
            funcName.startsWith("Fuzz") || funcName.startsWith("Example")) return null

        return funcName to (psiFile.virtualFile ?: return null)
    }
}
