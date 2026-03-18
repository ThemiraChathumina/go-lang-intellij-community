package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionName
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GoTestRunLineMarkerContributor : RunLineMarkerContributor(), DumbAware {
    override fun getInfo(element: PsiElement): Info? {
        val parent = element.parent
        if (parent !is GoFunctionName) return null

        val function = parent.parent as? GoFunctionDecl ?: return null
        val file = element.containingFile
        if (!file.name.endsWith("_test.go")) return null

        if (!GoTestRunConfigurationProducer.isTestFunction(function)) return null

        val testName = function.functionName.identifier.text
        val icon = when (GoTestResultsService.getInstance(element.project).getStatus(testName)) {
            GoTestResultsService.Status.PASSED  -> AllIcons.RunConfigurations.TestState.Green2
            GoTestResultsService.Status.FAILED  -> AllIcons.RunConfigurations.TestState.Red2
            GoTestResultsService.Status.SKIPPED -> AllIcons.RunConfigurations.TestState.Yellow2
            null                                -> AllIcons.RunConfigurations.TestState.Run
        }

        val actions = ExecutorAction.getActions()
        return Info(icon, { testName }, *actions)
    }
}
