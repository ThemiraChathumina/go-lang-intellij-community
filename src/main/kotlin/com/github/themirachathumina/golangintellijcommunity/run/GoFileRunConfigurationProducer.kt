package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class GoFileRunConfigurationProducer : LazyRunConfigurationProducer<GoRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        ConfigurationTypeUtil.findConfigurationType(GoRunConfigurationType::class.java).configurationFactories[0]

    override fun setupConfigurationFromContext(
        config: GoRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.psiLocation ?: return false
        val file = location.containingFile ?: return false

        if (!GoRunAnalyzer.isMainPackage(file) || !GoRunAnalyzer.hasFuncMain(file)) return false

        val virtualFile = file.virtualFile ?: return false

        config.runKind = GoRunKind.FILE
        config.targetPath = virtualFile.path
        config.workingDirectory = virtualFile.parent.path
        config.name = "Run ${file.name}"

        // --- CRITICAL FIX ---
        // Tell IntelliJ exactly what we clicked on so it can anchor the context menu
        sourceElement.set(file)

        return true
    }

    override fun isConfigurationFromContext(
        config: GoRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.psiLocation?.containingFile ?: return false
        return config.runKind == GoRunKind.FILE && config.targetPath == file.virtualFile?.path
    }
}