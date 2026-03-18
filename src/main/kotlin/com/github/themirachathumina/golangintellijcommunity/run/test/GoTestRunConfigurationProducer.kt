package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.*
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GoTestRunConfigurationProducer : LazyRunConfigurationProducer<GoTestRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        ConfigurationTypeUtil.findConfigurationType(GoTestRunConfigurationType::class.java).configurationFactories[0]

    override fun setupConfigurationFromContext(
        config: GoTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val element = location.psiElement
        val file = element.containingFile as? GoFile ?: return false

        if (!file.name.endsWith("_test.go")) return false
        val virtualFile = file.virtualFile ?: return false
        val packageDir = virtualFile.parent.path

        // 1. Check if we are on a table-driven subtest row
        val subtestInfo = getSubtestInfo(element)
        if (subtestInfo != null) {
            val (testName, subName, anchor) = subtestInfo
            config.scope = GoTestScope.FUNCTION
            config.targetPath = ".:$testName/$subName"
            config.workingDirectory = packageDir
            config.name = "Test $testName/$subName"
            sourceElement.set(anchor)
            return true
        }

        // 2. Check if we are on a test function declaration
        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isTestFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false

            if (testName == "TestMain") {
                // TestMain triggers a package run
                config.scope = GoTestScope.PACKAGE
                config.targetPath = "."
                config.workingDirectory = packageDir
                config.name = "Tests in ${file.name}"
                sourceElement.set(function.functionName?.identifier ?: function)
                return true
            }

            // Individual test function
            config.scope = GoTestScope.FUNCTION
            config.targetPath = ".:$testName"
            config.workingDirectory = packageDir
            config.name = "Test $testName"
            sourceElement.set(function.functionName?.identifier ?: function)
            return true
        }

        // 3. Default to running the whole file
        config.scope = GoTestScope.FILE
        config.targetPath = virtualFile.path
        config.workingDirectory = packageDir
        config.name = "Test ${file.name}"
        sourceElement.set(file)
        return true
    }

    override fun isConfigurationFromContext(
        config: GoTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.location ?: return false
        val element = location.psiElement
        val file = element.containingFile as? GoFile ?: return false
        if (!file.name.endsWith("_test.go")) return false
        val virtualFile = file.virtualFile ?: return false

        // 1. Match Subtest scope
        val subtestInfo = getSubtestInfo(element)
        if (subtestInfo != null) {
            val (testName, subName) = subtestInfo
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName/$subName"
        }

        // 2. Match Function scope
        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isTestFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName"
        }

        // 3. Match File scope
        if (config.scope == GoTestScope.FILE) {
            return config.targetPath == virtualFile.path
        }

        return false
    }

    companion object {
        fun isTestFunction(function: GoFunctionDecl): Boolean {
            val name = function.functionName?.identifier?.text ?: return false
            return name.startsWith("Test") || name.startsWith("Benchmark") ||
                    name.startsWith("Fuzz") || name.startsWith("Example")
        }

        /**
         * Identifies if an element is part of a struct row in a test table.
         * Returns Triple(TestFunctionName, SanitizedSubtestName, AnchorPsiElement)
         */
        fun getSubtestInfo(element: PsiElement): Triple<String, String, PsiElement>? {
            var current: PsiElement? = element
            var rowLiteral: GoLiteralValue? = null

            // Walk up to find a struct literal that is an element of an array/slice
            while (current != null && current !is GoFunctionDecl) {
                if (current is GoLiteralValue) {
                    val parentElement = current.parent as? GoElement
                    val keyedElement = parentElement?.parent as? GoKeyedElement
                    val elementList = keyedElement?.parent as? GoElementList
                    val sliceLiteral = elementList?.parent as? GoLiteralValue

                    if (sliceLiteral != null) {
                        rowLiteral = current
                        break
                    }
                }
                current = current.parent
            }

            if (rowLiteral == null) return null

            val function = PsiTreeUtil.getParentOfType(rowLiteral, GoFunctionDecl::class.java) ?: return null
            if (!isTestFunction(function)) return null
            val testName = function.functionName?.identifier?.text ?: return null

            // Find the first string literal defined in this struct (usually the test case name)
            val firstString = PsiTreeUtil.findChildrenOfType(rowLiteral, GoStringLiteral::class.java)
                .minByOrNull { it.textOffset } ?: return null

            val subName = firstString.text.trim('"', '`')
            if (subName.isBlank()) return null

            // `go test -json` sanitizes subtest names by converting spaces to underscores.
            // E.g., "division by zero" -> "division_by_zero"
            val sanitizedSubName = subName.replace(" ", "_")

            // We use the opening '{' of the struct row as the reliable anchor for gutter icons
            val anchor = rowLiteral.firstChild ?: firstString

            return Triple(testName, sanitizedSubName, anchor)
        }
    }
}