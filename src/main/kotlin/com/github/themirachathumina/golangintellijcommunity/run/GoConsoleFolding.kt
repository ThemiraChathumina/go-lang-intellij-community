package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.ConsoleFolding
import com.intellij.openapi.project.Project

class GoConsoleFolding : ConsoleFolding() {

    override fun shouldFoldLine(project: Project, line: String): Boolean {
        val trimmed = line.trim()
        
        // 1. Fold the headers we explicitly print in GoRunProcessHandler
        if (trimmed.startsWith("Building Go Application:") || 
            trimmed.startsWith("Running Application:")) {
            return true
        }
        
        // 2. Fold the command lines printed by the process handler.
        // Heuristic: Check if it looks like a go build command or a temp executable run.
        if (trimmed.contains("go build") || trimmed.contains("go.exe\" build")) {
            return true
        }
        
        // Heuristic: Temp build binaries usually follow this pattern
        if (trimmed.contains("go_build_") && (trimmed.endsWith(".exe") || !trimmed.contains(" "))) {
            return true
        }
        
        // 3. Fold empty lines that surround these headers to keep the block compact
        if (trimmed.isEmpty()) return true

        return false
    }

    override fun getPlaceholderText(project: Project, lines: List<String>): String {
        return " [Go Build/Run Details] "
    }
}
