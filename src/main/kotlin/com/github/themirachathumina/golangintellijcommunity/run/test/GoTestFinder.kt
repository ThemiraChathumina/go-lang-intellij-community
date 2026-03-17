package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testIntegration.TestFinder

class GoTestFinder : TestFinder {
    override fun findSourceElement(from: PsiElement): PsiElement? {
        return PsiTreeUtil.getParentOfType(from, GoFunctionDecl::class.java) ?: from.containingFile
    }

    override fun findTestsForClass(element: PsiElement): Collection<PsiElement> {
        val file = element.containingFile as? GoFile ?: return emptyList()
        if (file.name.endsWith("_test.go")) return emptyList()

        val testFileName = file.name.removeSuffix(".go") + "_test.go"
        val testFile = file.containingDirectory?.findFile(testFileName) as? GoFile ?: return emptyList()

        if (element is GoFunctionDecl) {
            val testName = "Test" + element.functionName.identifier.text
            val tests = PsiTreeUtil.findChildrenOfType(testFile, GoFunctionDecl::class.java)
            val matchingTest = tests.find { it.functionName.identifier.text == testName }
            if (matchingTest != null) return listOf(matchingTest)
        }

        return listOf(testFile)
    }

    override fun findClassesForTest(element: PsiElement): Collection<PsiElement> {
        val file = element.containingFile as? GoFile ?: return emptyList()
        if (!file.name.endsWith("_test.go")) return emptyList()

        val sourceFileName = file.name.removeSuffix("_test.go") + ".go"
        val sourceFile = file.containingDirectory?.findFile(sourceFileName) as? GoFile ?: return emptyList()

        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && GoTestRunConfigurationProducer.isTestFunction(function)) {
            val testName = function.functionName.identifier.text
            val sourceName = if (testName.startsWith("Test")) testName.removePrefix("Test") else null
            if (sourceName != null) {
                val functions = PsiTreeUtil.findChildrenOfType(sourceFile, GoFunctionDecl::class.java)
                val matchingSource = functions.find { it.functionName.identifier.text == sourceName }
                if (matchingSource != null) return listOf(matchingSource)
            }
        }

        return listOf(sourceFile)
    }

    override fun isTest(element: PsiElement): Boolean {
        val file = element.containingFile ?: return false
        return file.name.endsWith("_test.go")
    }
}
