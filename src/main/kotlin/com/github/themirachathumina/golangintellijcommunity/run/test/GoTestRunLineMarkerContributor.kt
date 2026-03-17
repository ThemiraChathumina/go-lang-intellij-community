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
        
        val actions = ExecutorAction.getActions()
        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            { "Run Test" },
            *actions
        )
    }
}
