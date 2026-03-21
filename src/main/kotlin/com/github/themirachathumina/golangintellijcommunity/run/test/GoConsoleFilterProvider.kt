package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.openapi.project.Project

/**
 * Provides [GoConsoleFilter] to every console in the project, including the
 * per-test output panes inside the SMTestRunner tool window.
 */
class GoConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> =
        arrayOf(GoConsoleFilter(project))
}
