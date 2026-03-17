package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionName
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

        // Check if we are on a function
        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isTestFunction(function)) {
            val testName = function.functionName.identifier.text
            
            if (testName == "TestMain") {
                // TestMain triggers a package run
                config.scope = GoTestScope.PACKAGE
                config.targetPath = "." 
                config.workingDirectory = packageDir
                config.name = "Tests in ${file.name}"
                sourceElement.set(function.functionName.identifier)
                return true
            }

            // Individual test function
            config.scope = GoTestScope.FUNCTION
            config.targetPath = ".:$testName"
            config.workingDirectory = packageDir
            config.name = "Test $testName"
            sourceElement.set(function.functionName.identifier)
            return true
        }

        // Default to running the whole file (which we've mapped to the package)
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
        
        // Match Function scope
        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
        if (function != null && isTestFunction(function)) {
            val testName = function.functionName.identifier.text
            return config.scope == GoTestScope.FUNCTION && config.targetPath == ".:$testName"
        }

        // Match File scope
        if (config.scope == GoTestScope.FILE) {
            return config.targetPath == virtualFile.path
        }

        return false
    }

    companion object {
        fun isTestFunction(function: GoFunctionDecl): Boolean {
            val name = function.functionName.identifier.text
            return name.startsWith("Test") || name.startsWith("Benchmark") || 
                   name.startsWith("Fuzz") || name.startsWith("Example")
        }
    }
}
