package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionName
import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunDialog
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class GoTestRunLineMarkerContributor : RunLineMarkerContributor(), DumbAware {
    override fun getInfo(element: PsiElement): Info? {
        val file = element.containingFile
        if (!file.name.endsWith("_test.go")) return null

        // 1. Gutter icon for Test / Benchmark / Fuzz Function Declarations
        val parent = element.parent
        if (parent is GoFunctionName) {
            val function = parent.parent as? GoFunctionDecl ?: return null
            val testName = function.functionName?.identifier?.text ?: return null

            return when {
                GoFuzzRunConfigurationProducer.isFuzzFunction(function) ->
                    createFuzzInfo(testName, element)
                GoTestRunConfigurationProducer.isTestFunction(function) ||
                GoBenchmarkRunConfigurationProducer.isBenchmarkFunction(function) ->
                    createInfo(testName, element)
                else -> null
            }
        }

        // 2. Gutter icon for Table-Driven Subtest Rows
        // We attach the icon to the opening '{' of the test case struct literal
        if (element.text == "{") {
            val subtestInfo = GoTestRunConfigurationProducer.getSubtestInfo(element)
            if (subtestInfo != null) {
                val (testName, subName, anchor) = subtestInfo
                if (element == anchor) {
                    return createInfo("$testName/$subName", element)
                }
            }
        }

        return null
    }

    private fun createInfo(fullTestName: String, element: PsiElement): Info {
        val icon = when (GoTestResultsService.getInstance(element.project).getStatus(fullTestName)) {
            GoTestResultsService.Status.PASSED  -> AllIcons.RunConfigurations.TestState.Green2
            GoTestResultsService.Status.FAILED  -> AllIcons.RunConfigurations.TestState.Red2
            GoTestResultsService.Status.SKIPPED -> AllIcons.RunConfigurations.TestState.Yellow2
            null                                -> AllIcons.RunConfigurations.TestState.Run
        }

        // Only include the Run and Run-with-Coverage executor actions.
        // ExecutorAction.getActions() returns all executor actions (Run, Debug, Coverage,
        // Profiler, …) plus the editRunConfigurations action. We whitelist by matching the
        // action's template icon against the singleton icons of the two executors we want.
        // This excludes Debug, Profiler, and the edit-configurations entry in one shot,
        // and avoids any duplicate "Modify Run Configuration..." entries.
        val registry = ExecutorRegistry.getInstance()
        val allowedIcons = setOfNotNull(
            registry.getExecutorById(DefaultRunExecutor.EXECUTOR_ID)?.icon,
            registry.getExecutorById(CoverageExecutor.EXECUTOR_ID)?.icon
        )
        val executorActions = ExecutorAction.getActions()
            .filter { it.templatePresentation.icon in allowedIcons }
            .map { GatedExecutorAction(it) }
        val actions = (executorActions + ModifyRunConfigAction()).toTypedArray()
        return Info(icon, { fullTestName }, *actions)
    }

    /**
     * Gutter icon for Fuzz functions. Instead of delegating to platform [ExecutorAction]s
     * (which would show a profiler option and no clean submenu), we build two explicit
     * submenus: "Run" and "Run with Coverage", each with two children:
     *  - run as a regular unit test  (go test -run FuzzXxx)
     *  - run with fuzzing enabled    (go test -fuzz FuzzXxx)
     */
    private fun createFuzzInfo(fuzzName: String, element: PsiElement): Info {
        val icon = when (GoTestResultsService.getInstance(element.project).getStatus(fuzzName)) {
            GoTestResultsService.Status.PASSED  -> AllIcons.RunConfigurations.TestState.Green2
            GoTestResultsService.Status.FAILED  -> AllIcons.RunConfigurations.TestState.Red2
            GoTestResultsService.Status.SKIPPED -> AllIcons.RunConfigurations.TestState.Yellow2
            null                                -> AllIcons.RunConfigurations.TestState.Run
        }

        val project = element.project
        val packageDir = element.containingFile.virtualFile?.parent?.path ?: project.basePath ?: ""

        val runGroup    = FuzzRunGroup(fuzzName, project, packageDir)
        val coverGroup  = FuzzCoverageGroup(fuzzName, project, packageDir)
        val modifyGroup = FuzzModifyGroup(fuzzName, project, packageDir)

        return Info(icon, { fuzzName }, runGroup, coverGroup, modifyGroup)
    }

    /**
     * Wraps a platform [ExecutorAction] so that [GoTestProducerGate] is bypassed on
     * both [update] and [actionPerformed].  Without this, the gate (which suppresses
     * producers for the context menu) also suppresses them for gutter icon clicks,
     * causing "Nothing here" to appear.
     */
    private class GatedExecutorAction(private val delegate: AnAction) : AnAction() {
        init { copyFrom(delegate) }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
        override fun update(e: AnActionEvent) =
            GoTestProducerGate.withBypass { delegate.update(e) }
        override fun actionPerformed(e: AnActionEvent) =
            GoTestProducerGate.withBypass { delegate.actionPerformed(e) }
    }

    /**
     * Gutter action that opens the Run/Debug Configurations dialog for the most
     * specific (function-level) configuration that matches the gutter element.
     */
    private class ModifyRunConfigAction : AnAction(
        "Modify Run Configuration\u2026", null, AllIcons.Actions.EditSource
    ) {
        override fun getActionUpdateThread() = ActionUpdateThread.BGT

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabledAndVisible = resolveConfig(e) != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val fromContext = resolveConfig(e) ?: return
            RunDialog.editConfiguration(project, fromContext.configurationSettings, "Edit configuration")
        }

        private fun resolveConfig(e: AnActionEvent) =
            GoTestProducerGate.withBypass {
                val context = ConfigurationContext.getFromContext(e.dataContext, e.place)
                val all = listOf(
                    GoTestRunConfigurationProducer(),
                    GoBenchmarkRunConfigurationProducer(),
                    GoFuzzRunConfigurationProducer()
                ).mapNotNull { it.findOrCreateConfigurationFromContext(context) }
                 .filter { it.configuration is GoTestRunConfiguration }

                // Prefer function-scope configs (most specific); fall back to any match
                all.firstOrNull { (it.configuration as? GoTestRunConfiguration)?.scope == GoTestScope.FUNCTION }
                    ?: all.firstOrNull()
            }
    }

    /** "Run" submenu for a fuzz function gutter icon. */
    private class FuzzRunGroup(
        private val fuzzName: String,
        private val project: Project,
        private val packageDir: String
    ) : ActionGroup("Run", true) {
        init { templatePresentation.icon = AllIcons.Actions.Execute }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
        override fun getChildren(e: AnActionEvent?): Array<AnAction> = arrayOf(
            FuzzExecuteAction(
                "gotest $fuzzName", fuzzName, GoTestKind.TEST,
                project, packageDir, DefaultRunExecutor.EXECUTOR_ID
            ),
            FuzzExecuteAction(
                "go test -fuzz $fuzzName ...", fuzzName, GoTestKind.FUZZ,
                project, packageDir, DefaultRunExecutor.EXECUTOR_ID
            )
        )
    }

    /** "Run with Coverage" submenu for a fuzz function gutter icon. */
    private class FuzzCoverageGroup(
        private val fuzzName: String,
        private val project: Project,
        private val packageDir: String
    ) : ActionGroup("Run with Coverage", true) {
        init { templatePresentation.icon = AllIcons.General.RunWithCoverage }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
        override fun getChildren(e: AnActionEvent?): Array<AnAction> = arrayOf(
            FuzzExecuteAction(
                "gotest $fuzzName", fuzzName, GoTestKind.TEST,
                project, packageDir, CoverageExecutor.EXECUTOR_ID
            ),
            FuzzExecuteAction(
                "go test -fuzz $fuzzName ...", fuzzName, GoTestKind.FUZZ,
                project, packageDir, CoverageExecutor.EXECUTOR_ID
            )
        )
    }

    /** "Modify Run Configuration…" submenu for a fuzz function gutter icon. */
    private class FuzzModifyGroup(
        private val fuzzName: String,
        private val project: Project,
        private val packageDir: String
    ) : ActionGroup("Modify Run Configuration\u2026", true) {
        init { templatePresentation.icon = AllIcons.Actions.EditSource }
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
        override fun getChildren(e: AnActionEvent?): Array<AnAction> = arrayOf(
            FuzzModifyAction("gotest $fuzzName", fuzzName, GoTestKind.TEST, project, packageDir),
            FuzzModifyAction("go test -fuzz $fuzzName ...", fuzzName, GoTestKind.FUZZ, project, packageDir)
        )
    }

    /** Opens the Edit Configuration dialog for one specific fuzz variant. */
    private class FuzzModifyAction(
        name: String,
        private val fuzzName: String,
        private val testKind: GoTestKind,
        private val project: Project,
        private val packageDir: String
    ) : AnAction(name, null, GoIcons.FILE) {
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
        override fun actionPerformed(e: AnActionEvent) {
            val factory = ConfigurationTypeUtil
                .findConfigurationType(GoTestRunConfigurationType::class.java)
                .configurationFactories[0]
            val configName = if (testKind == GoTestKind.FUZZ) "go test -fuzz $fuzzName ..." else "gotest $fuzzName"
            val settings = RunManager.getInstance(project).createConfiguration(configName, factory)
            val config   = settings.configuration as GoTestRunConfiguration
            config.testKind         = testKind
            config.scope            = GoTestScope.FUNCTION
            config.targetPath       = ".:$fuzzName"
            config.workingDirectory = packageDir
            config.name             = configName
            RunDialog.editConfiguration(project, settings, "Edit configuration")
        }
    }

    /**
     * Leaf action inside a fuzz gutter submenu. Creates a fresh [GoTestRunConfiguration]
     * on every invocation (not stored in RunManager) and executes it immediately.
     */
    private class FuzzExecuteAction(
        name: String,
        private val fuzzName: String,
        private val testKind: GoTestKind,
        private val project: Project,
        private val packageDir: String,
        private val executorId: String
    ) : AnAction(name, null, GoIcons.FILE) {
        override fun getActionUpdateThread() = ActionUpdateThread.BGT
        override fun actionPerformed(e: AnActionEvent) {
            val executor = ExecutorRegistry.getInstance().getExecutorById(executorId) ?: return
            val factory  = ConfigurationTypeUtil
                .findConfigurationType(GoTestRunConfigurationType::class.java)
                .configurationFactories[0]
            val configName = if (testKind == GoTestKind.FUZZ) "go test -fuzz $fuzzName ..." else "gotest $fuzzName"
            val settings = RunManager.getInstance(project).createConfiguration(configName, factory)
            val config   = settings.configuration as GoTestRunConfiguration
            config.testKind        = testKind
            config.scope           = GoTestScope.FUNCTION
            config.targetPath      = ".:$fuzzName"
            config.workingDirectory = packageDir
            config.name            = configName
            ProgramRunnerUtil.executeConfiguration(settings, executor)
        }
    }
}
