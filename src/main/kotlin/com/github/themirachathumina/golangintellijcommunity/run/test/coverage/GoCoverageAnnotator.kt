package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.SimpleCoverageAnnotator
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.io.File

/**
 * Coverage annotator for Go files, based on [SimpleCoverageAnnotator].
 *
 * Overrides BOTH overloads of [getFileCoverageInformationString]:
 *  - PsiFile variant  → called by the **Project view** tree
 *  - VirtualFile variant → called by the **Coverage tool window** column
 * In both cases we report Go's statement-based percentage (e.g. "8.3% statements")
 * from stats collected during parsing, instead of the integer line-count ratio
 * the platform would compute from the [LineData] array.
 *
 * [getDirCoverageInformationString] (VirtualFile variant) is also overridden so
 * directory rows in the Coverage tool window show the correct aggregate.
 */
@Service(Service.Level.PROJECT)
class GoCoverageAnnotator(private val project: Project) : SimpleCoverageAnnotator(project) {

    companion object {
        @JvmStatic
        fun getInstance(project: Project): GoCoverageAnnotator =
            project.getService(GoCoverageAnnotator::class.java)
    }

    // -------------------------------------------------------------------------
    // SimpleCoverageAnnotator hooks
    // -------------------------------------------------------------------------

    override fun fillInfoForUncoveredFile(file: File): FileCoverageInfo = FileCoverageInfo()

    override fun shouldCollectCoverageInsideLibraryDirs(): Boolean = false

    override fun getRoots(
        project: Project,
        dataManager: CoverageDataManager,
        suite: CoverageSuitesBundle
    ): Array<VirtualFile> =
        dataManager.doInReadActionIfProjectOpen<Array<VirtualFile>> {
            ProjectRootManager.getInstance(project).contentRoots
        } ?: VirtualFile.EMPTY_ARRAY

    // -------------------------------------------------------------------------
    // Statement-percentage display — Project view (PsiFile variant)
    // -------------------------------------------------------------------------

    override fun getFileCoverageInformationString(
        psiFile: PsiFile,
        bundle: CoverageSuitesBundle,
        manager: CoverageDataManager
    ): String? {
        val path = psiFile.virtualFile?.path ?: return super.getFileCoverageInformationString(psiFile, bundle, manager)
        return stmtString(path) ?: super.getFileCoverageInformationString(psiFile, bundle, manager)
    }

    // -------------------------------------------------------------------------
    // Statement-percentage display — Coverage tool window (VirtualFile variant)
    // -------------------------------------------------------------------------

    override fun getFileCoverageInformationString(
        project: Project,
        file: VirtualFile,
        bundle: CoverageSuitesBundle,
        manager: CoverageDataManager
    ): String? =
        stmtString(file.path)
            ?: super.getFileCoverageInformationString(project, file, bundle, manager)

    override fun getDirCoverageInformationString(
        project: Project,
        dir: VirtualFile,
        bundle: CoverageSuitesBundle,
        manager: CoverageDataManager
    ): String? {
        val basePath = project.basePath
            ?: return super.getDirCoverageInformationString(project, dir, bundle, manager)
        val prefix = dir.path + "/"
        val all = GoCoverageRunner.getAllStmtStats(basePath)
            ?: return super.getDirCoverageInformationString(project, dir, bundle, manager)

        var covered = 0; var total = 0
        for ((filePath, stats) in all) {
            if (filePath.startsWith(prefix)) { covered += stats.covered; total += stats.total }
        }
        return if (total == 0) null else formatStmt(covered, total)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun stmtString(filePath: String): String? {
        val basePath = project.basePath ?: return null
        val stats = GoCoverageRunner.getStmtStats(basePath, filePath) ?: return null
        return if (stats.total == 0) null else formatStmt(stats.covered, stats.total)
    }

    /** Formats as "X.X% statements" with one decimal place, matching `go test` output. */
    private fun formatStmt(covered: Int, total: Int): String {
        val tenths = covered * 1000 / total
        return "${tenths / 10}.${tenths % 10}% statements"
    }
}
