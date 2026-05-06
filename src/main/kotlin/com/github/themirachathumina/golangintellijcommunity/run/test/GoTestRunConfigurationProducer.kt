package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.*
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/*
 * Run configuration producer that creates a GoTestRunConfiguration from the current context.
 * Automatically detects whether to run a single test function, a subtest, an entire file, 
 * or a package directory based on the user's cursor position or selection.
 */
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

        // Handle directory right-click (project view folder or package node)
        if (element is PsiDirectory) {
            if (GoTestProducerGate.shouldSuppressDirectory(element)) return false
            val functions = GoTestProducerGate.getTestFunctionsInDirectory(element)
            val hasTests = functions.any {
                isTestFunction(it) && !GoFuzzRunConfigurationProducer.isFuzzFunction(it)
            }
            if (!hasTests) return false
            config.testKind         = GoTestKind.TEST
            config.scope            = GoTestScope.DIRECTORY
            config.targetPath       = element.virtualFile.path
            config.workingDirectory = element.virtualFile.path
            config.name             = "gotest ${element.name}"
            sourceElement.set(element)
            return true
        }

        val file = element.containingFile as? GoFile ?: return false

        if (!file.name.endsWith("_test.go")) return false
        if (GoTestProducerGate.shouldSuppress(file, element)) return false

        val virtualFile = file.virtualFile ?: return false
        val packageDir = virtualFile.parent.path

        val subtestInfo = getSubtestInfo(element)
        if (subtestInfo != null) {
            val (testName, subName, anchor) = subtestInfo
            if (testName.startsWith("Benchmark")) return false

            config.testKind = GoTestKind.TEST
            config.scope = GoTestScope.FUNCTION
            config.targetPath = ".:$testName/$subName"
            config.workingDirectory = packageDir
            config.name = "gotest $testName/$subName"
            sourceElement.set(anchor)
            return true
        }

        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isTestFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false

            // Let GoFuzzRunConfigurationProducer handle Fuzz functions exclusively
            if (testName.startsWith("Fuzz")) return false

            config.testKind = GoTestKind.TEST

            if (testName == "TestMain") {
                config.scope = GoTestScope.PACKAGE
                config.targetPath = "."
                config.workingDirectory = packageDir
                config.name = file.name
            } else {
                config.scope = GoTestScope.FUNCTION
                config.targetPath = ".:$testName"
                config.workingDirectory = packageDir
                // UI matches: Run 'gotest TestAdd'
                config.name = "gotest $testName"
            }
            sourceElement.set(function.functionName?.identifier ?: function)
            return true
        }

        // If the cursor is inside a function but it wasn't handled above, it is a
        // non-test function (e.g. BenchmarkXxx). Don't fall through to file-scope.
        if (function != null) return false

        val allFunctions = PsiTreeUtil.findChildrenOfType(file, GoFunctionDecl::class.java)
        if (allFunctions.none { isTestFunction(it) }) return false

        config.testKind = GoTestKind.TEST
        config.scope = GoTestScope.FILE
        config.targetPath = virtualFile.path
        config.workingDirectory = packageDir
        // UI matches: Run 'main_test.go'
        config.name = file.name
        sourceElement.set(file)
        return true
    }

    override fun isConfigurationFromContext(
        config: GoTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        if (config.testKind != GoTestKind.TEST) return false

        val location = context.location ?: return false
        val element = location.psiElement

        if (element is PsiDirectory) {
            return config.scope == GoTestScope.DIRECTORY &&
                   config.targetPath == element.virtualFile.path
        }

        val file = element.containingFile as? GoFile ?: return false
        if (!file.name.endsWith("_test.go")) return false
        val virtualFile = file.virtualFile ?: return false

        val subtestInfo = getSubtestInfo(element)
        if (subtestInfo != null) {
            val (testName, subName) = subtestInfo
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName/$subName"
        }

        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isTestFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName"
        }

        if (config.scope == GoTestScope.FILE) {
            return config.targetPath == virtualFile.path
        }

        return false
    }

    companion object {
        fun isTestFunction(function: GoFunctionDecl): Boolean {
            val name = function.functionName?.identifier?.text ?: return false
            return name.startsWith("Test") || name.startsWith("Fuzz") || name.startsWith("Example")
        }

        /** Returns true if [function] contains at least one `*.Run(...)` call site (i.e. `t.Run`). */
        private fun functionUsesSubtests(function: GoFunctionDecl): Boolean =
            PsiTreeUtil.findChildrenOfType(function, GoOperandName::class.java)
                .any { it.text.endsWith(".Run") }

        fun getSubtestInfo(element: PsiElement): Triple<String, String, PsiElement>? {
            var current: PsiElement? = element
            var rowLiteral: GoLiteralValue? = null

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
            val testName = function.functionName?.identifier?.text ?: return null

            // Only show subtest gutter icons when the function actually calls t.Run(...).
            // Without t.Run the loop iterations are not named subtests and cannot be targeted
            // individually with -run, so the icons would be misleading.
            if (!functionUsesSubtests(function)) return null

            val firstString = PsiTreeUtil.findChildrenOfType(rowLiteral, GoStringLiteral::class.java)
                .minByOrNull { it.textOffset } ?: return null

            val subName = firstString.text.trim('"', '`')
            if (subName.isBlank()) return null

            val sanitizedSubName = subName.replace(" ", "_")
            val anchor = rowLiteral.firstChild ?: firstString

            return Triple(testName, sanitizedSubName, anchor)
        }
    }
}