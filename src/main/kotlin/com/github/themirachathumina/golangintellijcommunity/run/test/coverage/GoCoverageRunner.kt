package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.intellij.coverage.*
import com.intellij.openapi.project.Project
import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Parses Go's native coverage profile format (`go test -coverprofile=file.out`) into
 * IntelliJ's [ProjectData] model, which drives the editor gutter line markers.
 *
 * Go coverage line format:
 *   github.com/user/pkg/file.go:startLine.startCol,endLine.endCol  numStmt  count
 * `count > 0` = covered, `count == 0` = not covered.
 */
class GoCoverageRunner : CoverageRunner() {

    data class StmtStats(val covered: Int, val total: Int)

    companion object {
        const val ID = "GoCoverageRunner"
        private val WHITESPACE_REGEX = Regex("\\s+")

        // Keyed by project base path → (absolute file path → statement stats).
        // Populated during parsing; read by GoCoverageAnnotator for decimal percentage display.
        private val stmtStatsByProject = ConcurrentHashMap<String, Map<String, StmtStats>>()

        fun getStmtStats(projectBasePath: String, filePath: String): StmtStats? =
            stmtStatsByProject[projectBasePath]?.get(filePath)

        fun getAllStmtStats(projectBasePath: String): Map<String, StmtStats>? =
            stmtStatsByProject[projectBasePath]
    }

    override fun getPresentableName(): String = "Go Coverage"
    override fun getId(): String = ID
    override fun getDataFileExtension(): String = "out"

    override fun acceptsCoverageEngine(engine: CoverageEngine): Boolean =
        engine is GoCoverageEngine

    override fun loadCoverageData(
        sessionDataFile: File,
        baseCoverageSuite: CoverageSuite?,
        reporter: CoverageLoadErrorReporter
    ): CoverageLoadingResult {
        if (!sessionDataFile.exists()) {
            return FailedCoverageLoadingResult("Coverage file does not exist: ${sessionDataFile.absolutePath}")
        }
        if (!sessionDataFile.canRead()) {
            return FailedCoverageLoadingResult("Cannot read coverage file: ${sessionDataFile.absolutePath}")
        }

        val project = baseCoverageSuite?.project
            ?: return FailedCoverageLoadingResult("Cannot load coverage data: project is null in suite")

        return try {
            val projectData = parseGoCoverageFile(sessionDataFile, project)
            SuccessCoverageLoadingResult(projectData)
        } catch (e: Exception) {
            FailedCoverageLoadingResult("Failed to parse coverage file: ${e.message}", e)
        }
    }

    // Keep the deprecated method around just in case older platform code calls it
    @Deprecated("Use loadCoverageData with reporter")
    override fun loadCoverageData(sessionDataFile: File, baseCoverageSuite: CoverageSuite?): ProjectData? {
        val project = baseCoverageSuite?.project ?: return null
        return try {
            parseGoCoverageFile(sessionDataFile, project)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseGoCoverageFile(file: File, project: Project): ProjectData {
        val projectData = ProjectData()
        val moduleRoot = GoModUtils.getModuleRootDir(project)
        val moduleName = GoModUtils.getModuleName(project)

        // lineMap: absolute file path → (lineNumber → hit count)
        // Used for gutter highlighting — every line in startLine..endLine is marked.
        // Storing the actual count (not just a boolean) lets LineData.setHits() drive
        // both the gutter colour and the "ran N times" tooltip correctly.
        val fileLines = mutableMapOf<String, MutableMap<Int, Int>>()

        // stmtAcc: absolute file path → (coveredStmts, totalStmts)
        // Tracks Go's statement-based counts separately so we can display the correct
        // percentage (e.g. 8.3%) instead of the line-count ratio (e.g. 8%).
        val fileStmtAcc = mutableMapOf<String, IntArray>() // [0]=covered, [1]=total

        file.forEachLine { rawLine ->
            val line = rawLine.trim()
            if (line.startsWith("mode:") || line.isEmpty()) return@forEachLine

            val colonIdx = line.lastIndexOf(':')
            if (colonIdx < 0) return@forEachLine

            val importPath = line.substring(0, colonIdx)
            val absolutePath = resolveFilePath(importPath, moduleName, moduleRoot, project)
                ?: return@forEachLine

            val rest = line.substring(colonIdx + 1)   // e.g. "10.2,15.3 4 1"
            val parts = rest.split(WHITESPACE_REGEX)
            if (parts.size < 3) return@forEachLine

            val numStmts = parts[1].toIntOrNull() ?: 1
            val count    = parts[2].toIntOrNull() ?: return@forEachLine
            val covered  = count > 0

            val rangeParts = parts[0].split(",")
            if (rangeParts.size != 2) return@forEachLine

            val startLine = rangeParts[0].substringBefore('.').toIntOrNull() ?: return@forEachLine
            val endLine   = rangeParts[1].substringBefore('.').toIntOrNull() ?: return@forEachLine

            // --- gutter: mark every line in the block's range ---
            // When multiple blocks touch the same line, keep the highest count so the
            // gutter tooltip ("ran N times") reflects actual executions.
            val lineMap = fileLines.getOrPut(absolutePath) { mutableMapOf() }
            for (lineNum in startLine..endLine) {
                lineMap[lineNum] = maxOf(lineMap[lineNum] ?: 0, count)
            }

            // --- stats: accumulate statement counts ---
            val acc = fileStmtAcc.getOrPut(absolutePath) { IntArray(2) }
            acc[1] += numStmts
            if (covered) acc[0] += numStmts
        }

        // Store statement stats so GoCoverageAnnotator can display the correct percentage.
        project.basePath?.let { basePath ->
            stmtStatsByProject[basePath] = fileStmtAcc.mapValues { (_, acc) ->
                StmtStats(covered = acc[0], total = acc[1])
            }
        }

        // Build ProjectData from the line map (gutter data).
        for ((absolutePath, lineMap) in fileLines) {
            val classData: ClassData = projectData.getOrCreateClassData(absolutePath)
            val maxLine = lineMap.keys.maxOrNull() ?: continue

            val linesArray = arrayOfNulls<LineData>(maxLine + 1)
            for ((lineNum, hits) in lineMap) {
                val lineData = LineData(lineNum, "")
                // setHits() updates both the hit counter (shown in tooltip) and the
                // status byte (FULL when hits > 0, NONE when hits == 0) automatically.
                lineData.setHits(hits)
                linesArray[lineNum] = lineData
            }

            @Suppress("UNCHECKED_CAST")
            classData.setLines(linesArray as Array<LineData>)
        }

        return projectData
    }

    private fun resolveFilePath(importPath: String, moduleName: String?, moduleRoot: File?, project: Project): String? {
        if (moduleName != null && moduleRoot != null && importPath.startsWith("$moduleName/")) {
            val relativePath = importPath.substring(moduleName.length + 1)
            val resolvedFile = File(moduleRoot, relativePath)
            if (resolvedFile.exists()) {
                return resolvedFile.absolutePath.replace('\\', '/')
            }
        }

        val projectBase = project.basePath
        if (projectBase != null) {
            val parts = importPath.split('/')
            for (i in parts.indices) {
                val subPath = parts.subList(i, parts.size).joinToString("/")
                val candidate = File(projectBase, subPath)
                if (candidate.exists()) {
                    return candidate.absolutePath.replace('\\', '/')
                }
            }
        }

        return null
    }
}
