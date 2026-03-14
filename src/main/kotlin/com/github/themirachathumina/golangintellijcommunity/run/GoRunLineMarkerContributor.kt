package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionName
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class GoRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        // 1. We only want to attach the icon to the leaf identifier node.
        // Based on your PSI tree, the text "main" is a child of GoFunctionName.
        val parent = element.parent
        if (parent !is GoFunctionName) return null

        // 2. Ensure the function is actually named "main"
        if (element.text != "main") return null

        // 3. Ensure the file belongs to `package main`
        val file = element.containingFile ?: return null
        if (!GoRunAnalyzer.isMainPackage(file)) return null

        // 4. Fetch the standard IDE Run/Debug actions
        val actions = ExecutorAction.getActions()

        // 5. Return the green Play icon, a tooltip, and the executable actions
        return Info(
            AllIcons.RunConfigurations.TestState.Run,
            { "Run '${file.name}'" },
            *actions
        )
    }
}