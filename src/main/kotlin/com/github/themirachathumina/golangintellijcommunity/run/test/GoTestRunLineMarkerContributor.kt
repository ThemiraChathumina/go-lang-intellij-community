package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionName
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement

class GoTestRunLineMarkerContributor : RunLineMarkerContributor(), DumbAware {
    override fun getInfo(element: PsiElement): Info? {
        val file = element.containingFile
        if (!file.name.endsWith("_test.go")) return null

        // 1. Gutter icon for Test Function Declarations
        val parent = element.parent
        if (parent is GoFunctionName) {
            val function = parent.parent as? GoFunctionDecl ?: return null
            if (GoTestRunConfigurationProducer.isTestFunction(function)) {
                val testName = function.functionName?.identifier?.text ?: return null
                return createInfo(testName, element)
            }
        }

        // 2. Gutter icon for Table-Driven Subtest Rows
        // We attach the icon to the opening '{' of the test case struct literal
        if (element.text == "{") {
            val subtestInfo = GoTestRunConfigurationProducer.getSubtestInfo(element)
            if (subtestInfo != null) {
                val (testName, subName, anchor) = subtestInfo

                // Only render if the current element is exactly our designated anchor
                if (element == anchor) {
                    val fullTestName = "$testName/$subName"
                    return createInfo(fullTestName, element)
                }
            }
        }

        return null
    }

    private fun createInfo(fullTestName: String, element: PsiElement): Info {
        val icon = when (GoTestResultsService.getInstance(element.project).getStatus(fullTestName)) {
            GoTestResultsService.Status.PASSED  -> AllIcons.RunConfigurations.TestState.Green2
            GoTestResultsService.Status.FAILED  -> AllIcons.RunConfigurations.TestState.Red2
            GoTestResultsService.Status.SKIPPED -> AllIcons.RunConfigurations.TestState.Yellow2
            null                                -> AllIcons.RunConfigurations.TestState.Run
        }

        val actions = ExecutorAction.getActions()
        return Info(icon, { fullTestName }, *actions)
    }
}