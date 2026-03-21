package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.github.themirachathumina.golangintellijcommunity.run.test.GoBenchmarkRunConfigurationProducer
import com.github.themirachathumina.golangintellijcommunity.run.test.GoFuzzRunConfigurationProducer
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestKind
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestProducerGate
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfiguration
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfigurationProducer
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfigurationType
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestScope
import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil

class GoTestCoverageContextGroup : ActionGroup(), DumbAware {

    init {
        isPopup = true
        templatePresentation.text = "Run with Coverage"
        templatePresentation.icon = AllIcons.General.RunWithCoverage
    }

    // update() runs on BGT (PSI access is allowed there via PreCachedDataContext).
    // getChildren() runs on EDT and must not touch PSI — it reads cached data only.
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    private data class FuzzInfo(val fuzzName: String, val packageDir: String, val project: Project)

    @Volatile private var cachedConfigs: List<ConfigurationFromContext> = emptyList()
    @Volatile private var cachedFuzzInfo: FuzzInfo? = null

    override fun update(e: AnActionEvent) {
        val context = ConfigurationContext.getFromContext(e.dataContext, e.place)
        val element  = context.location?.psiElement
        val project  = e.project

        if (element != null && project != null) {
            val fn = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)
            if (fn != null && GoFuzzRunConfigurationProducer.isFuzzFunction(fn)) {
                val fuzzName   = fn.functionName?.identifier?.text
                val packageDir = element.containingFile?.virtualFile?.parent?.path
                if (fuzzName != null && packageDir != null) {
                    cachedFuzzInfo = FuzzInfo(fuzzName, packageDir, project)
                    cachedConfigs  = emptyList()
                    e.presentation.isEnabledAndVisible = true
                    return
                }
            }
        }

        cachedFuzzInfo = null
        val configs = resolveTestConfigs(e)
        cachedConfigs = configs
        e.presentation.isEnabledAndVisible = configs.size >= 2
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val fuzz = cachedFuzzInfo
        if (fuzz != null) {
            return arrayOf(
                fuzzCoverageAction("gotest ${fuzz.fuzzName}", fuzz.fuzzName, GoTestKind.TEST, fuzz.project, fuzz.packageDir),
                fuzzCoverageAction("go test -fuzz ${fuzz.fuzzName} ...", fuzz.fuzzName, GoTestKind.FUZZ, fuzz.project, fuzz.packageDir)
            )
        }

        val configs = cachedConfigs
        return configs.map { fromContext ->
            val config   = fromContext.configuration as GoTestRunConfiguration
            val settings = fromContext.configurationSettings
            object : AnAction(config.name.replace("_", "__"), null, GoIcons.FILE) {
                override fun getActionUpdateThread() = ActionUpdateThread.BGT
                override fun actionPerformed(e: AnActionEvent) {
                    val executor = ExecutorRegistry.getInstance()
                        .getExecutorById(CoverageExecutor.EXECUTOR_ID) ?: return
                    ProgramRunnerUtil.executeConfiguration(settings, executor)
                }
            }
        }.toTypedArray()
    }

    private fun fuzzCoverageAction(
        name: String, fuzzName: String, testKind: GoTestKind, project: Project, packageDir: String
    ): AnAction = object : AnAction(name.replace("_", "__"), null, GoIcons.FILE) {
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
        override fun actionPerformed(e: AnActionEvent) {
            val executor = ExecutorRegistry.getInstance()
                .getExecutorById(CoverageExecutor.EXECUTOR_ID) ?: return
            val factory  = ConfigurationTypeUtil
                .findConfigurationType(GoTestRunConfigurationType::class.java)
                .configurationFactories[0]
            val settings = RunManager.getInstance(project).createConfiguration(name, factory)
            val config   = settings.configuration as GoTestRunConfiguration
            config.testKind         = testKind
            config.scope            = GoTestScope.FUNCTION
            config.targetPath       = ".:$fuzzName"
            config.workingDirectory = packageDir
            config.name             = name
            ProgramRunnerUtil.executeConfiguration(settings, executor)
        }
    }

    private fun resolveTestConfigs(e: AnActionEvent): List<ConfigurationFromContext> {
        val context = ConfigurationContext.getFromContext(e.dataContext, e.place)
        // Use fresh producer instances to bypass the ConfigurationContext cache that
        // the platform already populated (with empty results due to the gate).
        return GoTestProducerGate.withBypass {
            listOf(
                GoTestRunConfigurationProducer(),
                GoBenchmarkRunConfigurationProducer(),
                GoFuzzRunConfigurationProducer()
            ).mapNotNull { it.findOrCreateConfigurationFromContext(context) }
             .filter { it.configuration is GoTestRunConfiguration }
        }
    }
}
