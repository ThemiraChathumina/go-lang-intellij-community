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

class GoFuzzRunConfigurationProducer : LazyRunConfigurationProducer<GoTestRunConfiguration>() {

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
        if (GoTestProducerGate.shouldSuppress(file, element)) return false

        val virtualFile = file.virtualFile ?: return false
        val packageDir = virtualFile.parent.path

        // Fuzzing doesn't apply to subtests
        val subtestInfo = GoTestRunConfigurationProducer.getSubtestInfo(element)
        if (subtestInfo != null) return false

        // Fuzzing strictly applies to individual Fuzz target functions
        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isFuzzFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false
            config.testKind = GoTestKind.FUZZ
            config.scope = GoTestScope.FUNCTION
            config.targetPath = ".:$testName"
            config.workingDirectory = packageDir
            // UI matches: Run 'go test -fuzz FuzzReverse ...'
            config.name = "go test -fuzz $testName ..."
            sourceElement.set(function.functionName?.identifier ?: function)
            return true
        }

        // If inside a non-fuzz function don't fall through to file-scope
        if (function != null) return false

        // File-scope: offer "go test -fuzz <file>" when the file contains fuzz functions
        val allFunctions = PsiTreeUtil.findChildrenOfType(file, GoFunctionDecl::class.java)
        if (allFunctions.none { isFuzzFunction(it) }) return false

        config.testKind = GoTestKind.FUZZ
        config.scope = GoTestScope.FILE
        config.targetPath = virtualFile.path
        config.workingDirectory = packageDir
        config.name = "go test -fuzz ${file.name}"
        sourceElement.set(file)
        return true
    }

    override fun isConfigurationFromContext(
        config: GoTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        if (config.testKind != GoTestKind.FUZZ) return false

        val location = context.location ?: return false
        val element = location.psiElement
        val file = element.containingFile as? GoFile ?: return false
        if (!file.name.endsWith("_test.go")) return false

        val subtestInfo = GoTestRunConfigurationProducer.getSubtestInfo(element)
        if (subtestInfo != null) return false

        val virtualFile = file.virtualFile ?: return false

        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isFuzzFunction(function)) {
            val testName = function.functionName?.identifier?.text ?: return false
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName"
        }

        if (config.scope == GoTestScope.FILE) {
            return config.targetPath == virtualFile.path
        }

        return false
    }

    companion object {
        fun isFuzzFunction(function: GoFunctionDecl): Boolean {
            val name = function.functionName?.identifier?.text ?: return false
            return name.startsWith("Fuzz")
        }
    }
}