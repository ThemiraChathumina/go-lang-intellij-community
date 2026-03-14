package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.ConsoleFolding
import com.intellij.openapi.project.Project

class GoConsoleFolding : ConsoleFolding() {

    override fun shouldFoldLine(project: Project, line: String): Boolean {
        // We fold based on the exact structure we produce in GoRunProcessHandler
        
        // 1. Indented lines (command lines and build output)
        if (line.startsWith("  ")) {
            return true
        }
        
        val trimmed = line.trim()
        
        // 2. Main headers
        if (trimmed == "Building Go Application:" || 
            trimmed == "Running Application:") {
            return true
        }
        
        // 3. Status messages at the end of build/run
        if (trimmed.startsWith("Build finished with exit code") || 
            trimmed.startsWith("Process finished with exit code")) {
            return true
        }

        return false
    }

    override fun getPlaceholderText(project: Project, lines: List<String>): String {
        return " [Go Build/Run Details] "
    }
}
