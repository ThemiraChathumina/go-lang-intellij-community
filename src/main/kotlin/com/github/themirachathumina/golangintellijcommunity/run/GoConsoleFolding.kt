package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.ConsoleFolding
import com.intellij.openapi.project.Project

class GoConsoleFolding : ConsoleFolding() {

    override fun shouldFoldLine(project: Project, line: String): Boolean {
        // Fold the indented setup lines emitted by GoRunProcessHandler
        return line.startsWith("  ")
    }

    override fun getPlaceholderText(project: Project, lines: List<String>): String {
        return "<${lines.size} go setup calls>"
    }
}
