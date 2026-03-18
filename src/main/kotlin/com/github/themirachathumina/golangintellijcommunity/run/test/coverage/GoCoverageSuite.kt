package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.intellij.coverage.*

/**
 * Concrete [BaseCoverageSuite] for Go. The only thing [BaseCoverageSuite] leaves
 * unimplemented is [getCoverageEngine]; everything else is handled by the base class.
 */
class GoCoverageSuite(
    name: String,
    project: com.intellij.openapi.project.Project?,
    runner: CoverageRunner?,
    fileProvider: CoverageFileProvider?,
    timestamp: Long
) : BaseCoverageSuite(name, project, runner, fileProvider, timestamp) {

    override fun getCoverageEngine(): CoverageEngine = GoCoverageEngine.getInstance()
}
