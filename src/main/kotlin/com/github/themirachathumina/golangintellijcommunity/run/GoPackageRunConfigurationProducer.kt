package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement

class GoPackageRunConfigurationProducer : LazyRunConfigurationProducer<GoRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        ConfigurationTypeUtil.findConfigurationType(GoRunConfigurationType::class.java).configurationFactories[0]

    override fun setupConfigurationFromContext(
        config: GoRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.psiLocation ?: return false

        val directory: PsiDirectory = when (location) {
            is PsiDirectory -> location
            else -> location.containingFile?.containingDirectory ?: return false
        }

        val mainFiles = directory.files.filter { GoRunAnalyzer.isMainPackage(it) }

        if (location is PsiDirectory) {
            if (mainFiles.isEmpty()) return false
        } else {
            if (mainFiles.size <= 1) return false
        }

        val virtualDir = directory.virtualFile

        config.runKind = GoRunKind.PACKAGE
        config.targetPath = virtualDir.path
        config.workingDirectory = virtualDir.path
        config.name = "Run package '${directory.name}'"

        // --- CRITICAL FIX ---
        sourceElement.set(directory)

        return true
    }

    override fun isConfigurationFromContext(
        config: GoRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.psiLocation ?: return false
        val directory = if (location is PsiDirectory) location else location.containingFile?.containingDirectory ?: return false
        return config.runKind == GoRunKind.PACKAGE && config.targetPath == directory.virtualFile.path
    }
}