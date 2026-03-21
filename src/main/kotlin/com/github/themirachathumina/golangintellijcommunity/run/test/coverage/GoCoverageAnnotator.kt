package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.SimpleCoverageAnnotator
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
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

    // Return null so files NOT present in the coverage profile are never annotated.
    override fun fillInfoForUncoveredFile(file: File): FileCoverageInfo? = null

    override fun shouldCollectCoverageInsideLibraryDirs(): Boolean = false

    // Only return the immediate parent directories of files that actually have coverage
    // data.  SimpleCoverageAnnotator scans all files under the returned roots, so
    // returning the whole project root would annotate every .go file in the project.
    override fun getRoots(
        project: Project,
        dataManager: CoverageDataManager,
        suite: CoverageSuitesBundle
    ): Array<VirtualFile> =
        dataManager.doInReadActionIfProjectOpen {
            val basePath = project.basePath ?: return@doInReadActionIfProjectOpen emptyArray()
            val stats = GoCoverageRunner.getAllStmtStats(basePath) ?: return@doInReadActionIfProjectOpen emptyArray()
            val lfs = LocalFileSystem.getInstance()
            stats.keys
                .mapNotNull { path -> lfs.findFileByPath(path)?.parent }
                .distinctBy { it.path }
                .toTypedArray()
        } ?: emptyArray()

    // -------------------------------------------------------------------------
    // Statement-percentage display — Project view (PsiFile variant)
    // -------------------------------------------------------------------------

    override fun getFileCoverageInformationString(
        psiFile: PsiFile,
        bundle: CoverageSuitesBundle,
        manager: CoverageDataManager
    ): String? = stmtString(psiFile.virtualFile?.path ?: return null)

    // -------------------------------------------------------------------------
    // Statement-percentage display — Coverage tool window (VirtualFile variant)
    // -------------------------------------------------------------------------

    override fun getFileCoverageInformationString(
        project: Project,
        file: VirtualFile,
        bundle: CoverageSuitesBundle,
        manager: CoverageDataManager
    ): String? = stmtString(file.path)

    override fun getDirCoverageInformationString(
        project: Project,
        dir: VirtualFile,
        bundle: CoverageSuitesBundle,
        manager: CoverageDataManager
    ): String? {
        val basePath = project.basePath ?: return null
        val prefix = dir.path + "/"
        val all = GoCoverageRunner.getAllStmtStats(basePath) ?: return null

        // A file counts as "covered" when at least one of its statements was executed.
        var coveredFiles = 0; var totalFiles = 0
        var coveredStmts = 0; var totalStmts = 0
        for ((filePath, stats) in all) {
            if (filePath.startsWith(prefix)) {
                totalFiles++
                if (stats.covered > 0) coveredFiles++
                coveredStmts += stats.covered
                totalStmts  += stats.total
            }
        }
        if (totalFiles == 0) return null
        val filesPct  = coveredFiles * 100 / totalFiles
        val stmtsPct  = if (totalStmts > 0) coveredStmts * 100 / totalStmts else 0
        return "$filesPct% files, $stmtsPct% statements"
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
