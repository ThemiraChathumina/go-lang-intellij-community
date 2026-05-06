package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestProducerGate
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement

/*
 * Run configuration producer that creates a GoRunConfiguration when the user right-clicks
 * a directory containing a main package. Defers to test producers when the context is a test directory.
 */
class GoPackageRunConfigurationProducer : LazyRunConfigurationProducer<GoRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        ConfigurationTypeUtil.findConfigurationType(GoRunConfigurationType::class.java).configurationFactories[0]

    override fun setupConfigurationFromContext(
        config: GoRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val location = context.psiLocation ?: return false

        // Don't offer "Run package" when right-clicking a test file — the test
        // producers handle that context instead.
        if (location.containingFile?.name?.endsWith("_test.go") == true) return false

        val directory: PsiDirectory = when (location) {
            is PsiDirectory -> location
            else -> location.containingFile?.containingDirectory ?: return false
        }

        // When right-clicking a directory that also has test functions, the test context
        // groups take over and include the run config inside their submenus. Suppress the
        // flat "Run package" action so it doesn't appear as a duplicate alongside the submenus.
        if (location is PsiDirectory && !GoTestProducerGate.isBypassed()
            && GoTestProducerGate.shouldSuppressDirectory(directory)) return false

        val mainFiles = directory.files.filter { GoRunAnalyzer.isMainPackage(it) }

        if (mainFiles.none { GoRunAnalyzer.hasFuncMain(it) }) return false

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