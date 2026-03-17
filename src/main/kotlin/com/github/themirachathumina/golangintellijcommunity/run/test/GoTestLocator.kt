package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.github.themirachathumina.golangintellijcommunity.GoFileType

/**
 * Locator for Go tests.
 * Resolves URLs like 'gotest://package/test' to the test function declaration.
 */
object GoTestLocator : SMTestLocator {
    override fun getLocation(
        protocol: String,
        path: String,
        project: Project,
        scope: GlobalSearchScope
    ): List<Location<PsiElement>> {
        if (protocol != "gotest") return emptyList()

        // path is expected to be "packageName:testName"
        val parts = path.split(":")
        val testName = if (parts.size == 2) parts[1] else parts[0]
        
        val results = mutableListOf<Location<PsiElement>>()

        // Find all Go files in the project and search for the test function.
        // This is a basic implementation that can be improved with indices.
        val files = FileTypeIndex.getFiles(GoFileType, scope)
        val psiManager = PsiManager.getInstance(project)
        
        for (file in files) {
            val psiFile = psiManager.findFile(file) as? GoFile ?: continue
            val functions = PsiTreeUtil.findChildrenOfType(psiFile, GoFunctionDecl::class.java)
            for (function in functions) {
                if (function.functionName.identifier.text == testName) {
                    results.add(PsiLocation.fromPsiElement(function))
                }
            }
        }
        
        return results
    }
}
