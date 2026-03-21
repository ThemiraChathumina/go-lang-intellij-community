package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.run.GoRunConfiguration
import com.github.themirachathumina.golangintellijcommunity.run.test.GoTestRunConfiguration
import com.intellij.coverage.*
import com.intellij.coverage.view.CoverageViewExtension

import com.intellij.coverage.view.DirectoryCoverageViewExtension
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.io.File

/**
 * Ties Go's native coverage tooling into IntelliJ's coverage framework.
 * Registered via the `com.intellij.coverageEngine` extension point.
 */
class GoCoverageEngine : CoverageEngine() {

    companion object {
        @JvmStatic
        fun getInstance(): GoCoverageEngine =
            EP_NAME.extensionList.filterIsInstance<GoCoverageEngine>().first()

        fun isProductionGoFile(vFile: VirtualFile): Boolean =
            vFile.extension == "go" && !vFile.name.endsWith("_test.go")

        fun isProductionGoFile(psiFile: PsiFile): Boolean =
            isProductionGoFile(psiFile.virtualFile ?: return false)
    }

    override fun getPresentableText(): String = "Go Coverage"

    override fun isApplicableTo(conf: RunConfigurationBase<*>): Boolean =
        conf is GoTestRunConfiguration || conf is GoRunConfiguration

    override fun createCoverageEnabledConfiguration(
        conf: RunConfigurationBase<*>
    ): CoverageEnabledConfiguration = GoCoverageEnabledConfiguration(conf)

    override fun createCoverageSuite(
        name: String,
        project: Project,
        runner: CoverageRunner,
        fileProvider: CoverageFileProvider,
        timestamp: Long
    ): CoverageSuite? {
        return GoCoverageSuite(name, project, runner, fileProvider, timestamp)
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun createCoverageSuite(
        name: String,
        project: Project,
        runner: CoverageRunner,
        fileProvider: CoverageFileProvider,
        timestamp: Long,
        config: CoverageEnabledConfiguration
    ): CoverageSuite? {
        return GoCoverageSuite(name, project, runner, fileProvider, timestamp)
    }

    override fun createCoverageSuite(
        runner: CoverageRunner,
        name: String,
        fileProvider: CoverageFileProvider,
        filters: Array<out String>?,
        lastTimestamp: Long,
        suiteToMerge: String?,
        coverageByTestEnabled: Boolean,
        tracingEnabled: Boolean,
        trackTestFolders: Boolean,
        project: Project?
    ): CoverageSuite? {
        return GoCoverageSuite(name, project, runner, fileProvider, lastTimestamp)
    }

    override fun createEmptyCoverageSuite(runner: CoverageRunner): CoverageSuite =
        GoCoverageSuite("", null, runner, null, System.currentTimeMillis())

    override fun getCoverageAnnotator(project: Project): CoverageAnnotator =
        GoCoverageAnnotator.getInstance(project)

    /**
     * Returns a [DirectoryCoverageViewExtension] so the Coverage tool window opens
     * and shows a file/directory tree with coverage percentages.
     */
    override fun createCoverageViewExtension(
        project: Project,
        suites: CoverageSuitesBundle
    ): CoverageViewExtension =
        DirectoryCoverageViewExtension(project, GoCoverageAnnotator.getInstance(project), suites)

    /**
     * Allows coverage percentages to appear next to Go source files in the Project tree.
     * We only show stats for production files.
     */
    override fun coverageProjectViewStatisticsApplicableTo(vFile: VirtualFile): Boolean =
        !vFile.isDirectory && isProductionGoFile(vFile)

    override fun coverageEditorHighlightingApplicableTo(psiFile: PsiFile): Boolean =
        isProductionGoFile(psiFile)

    override fun acceptedByFilters(psiFile: PsiFile, suite: CoverageSuitesBundle): Boolean =
        isProductionGoFile(psiFile)

    // -------------------------------------------------------------------------
    // Source file ↔ coverage-data key mapping
    // -------------------------------------------------------------------------

    override fun getQualifiedName(outputFile: File, sourceFile: PsiFile): String? {
        return sourceFile.virtualFile?.path
    }

    override fun getQualifiedNames(sourceFile: PsiFile): Set<String> {
        val vFile = sourceFile.virtualFile ?: return emptySet()
        if (vFile.extension != "go") return emptySet()

        return setOf(vFile.path)
    }
}
