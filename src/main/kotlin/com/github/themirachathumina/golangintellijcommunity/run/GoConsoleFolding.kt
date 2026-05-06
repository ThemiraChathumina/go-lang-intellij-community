package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.ConsoleFolding
import com.intellij.openapi.project.Project

/*
 * Folds internal Go setup log lines (tagged with #gosetup) in the run console
 * so only user-visible output is shown by default.
 */
class GoConsoleFolding : ConsoleFolding() {

    // Both GoRunProcessHandler and GoTestProcessHandler tag their setup lines with
    // "#gosetup" so we can fold them without accidentally catching indented test-log
    // lines like "    main_test.go:58: ..." which also start with spaces.
    override fun shouldFoldLine(project: Project, line: String): Boolean =
        line.trimEnd().endsWith("#gosetup")

    override fun getPlaceholderText(project: Project, lines: List<String>): String =
        "<${lines.size} go setup calls>"
}
