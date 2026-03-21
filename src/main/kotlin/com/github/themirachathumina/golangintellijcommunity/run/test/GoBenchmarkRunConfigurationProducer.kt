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

class GoBenchmarkRunConfigurationProducer : LazyRunConfigurationProducer<GoTestRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        ConfigurationTypeUtil.findConfigurationType(GoTestRunConfigurationType::class.java).configurationFactories[0]

    override fun setupConfigurationFromContext(
        config: GoTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.location ?: return false
        val element = location.psiElement

        if (element is PsiDirectory) {
            if (GoTestProducerGate.shouldSuppressDirectory(element)) return false
            val functions = GoTestProducerGate.getTestFunctionsInDirectory(element)
            if (functions.none { isBenchmarkFunction(it) }) return false
            config.testKind         = GoTestKind.BENCHMARK
            config.scope            = GoTestScope.DIRECTORY
            config.targetPath       = element.virtualFile.path
            config.workingDirectory = element.virtualFile.path
            config.name             = "gobench ${element.name}"
            sourceElement.set(element)
            return true
        }

        val file = element.containingFile as? GoFile ?: return false

        if (!file.name.endsWith("_test.go")) return false
        if (GoTestProducerGate.shouldSuppress(file, element)) return false

        val virtualFile = file.virtualFile ?: return false
        val packageDir = virtualFile.parent.path

        val subtestInfo = GoTestRunConfigurationProducer.getSubtestInfo(element)
        if (subtestInfo != null) {
            val (testName, subName, anchor) = subtestInfo
            if (!testName.startsWith("Benchmark")) return false

            config.testKind = GoTestKind.BENCHMARK
            config.scope = GoTestScope.FUNCTION
            config.targetPath = ".:$testName/$subName"
            config.workingDirectory = packageDir
            config.name = "gobench $testName/$subName"
            sourceElement.set(anchor)
            return true
        }

        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isBenchmarkFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false
            config.testKind = GoTestKind.BENCHMARK
            config.scope = GoTestScope.FUNCTION
            config.targetPath = ".:$testName"
            config.workingDirectory = packageDir
            config.name = "gobench $testName"
            sourceElement.set(function.functionName?.identifier ?: function)
            return true
        }

        // If the cursor is inside a function but it wasn't handled above, it is a
        // non-benchmark function (e.g. TestXxx, FuzzXxx). Don't fall through to file-scope.
        if (function != null) return false

        val allFunctions = PsiTreeUtil.findChildrenOfType(file, GoFunctionDecl::class.java)
        if (allFunctions.none { isBenchmarkFunction(it) }) return false

        config.testKind = GoTestKind.BENCHMARK
        config.scope = GoTestScope.FILE
        config.targetPath = virtualFile.path
        config.workingDirectory = packageDir
        // UI matches: Run 'gobench main_test.go...'
        config.name = "gobench ${file.name}..."
        sourceElement.set(file)
        return true
    }

    override fun isConfigurationFromContext(
        config: GoTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        if (config.testKind != GoTestKind.BENCHMARK) return false

        val location = context.location ?: return false
        val element = location.psiElement

        if (element is PsiDirectory) {
            return config.scope == GoTestScope.DIRECTORY &&
                   config.targetPath == element.virtualFile.path
        }

        val file = element.containingFile as? GoFile ?: return false
        if (!file.name.endsWith("_test.go")) return false
        val virtualFile = file.virtualFile ?: return false

        val subtestInfo = GoTestRunConfigurationProducer.getSubtestInfo(element)
        if (subtestInfo != null) {
            val (testName, subName) = subtestInfo
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName/$subName"
        }

        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isBenchmarkFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName"
        }

        if (config.scope == GoTestScope.FILE) {
            return config.targetPath == virtualFile.path
        }

        return false
    }

    companion object {
        fun isBenchmarkFunction(function: GoFunctionDecl): Boolean {
            val name = function.functionName?.identifier?.text ?: return false
            return name.startsWith("Benchmark")
        }
    }
}