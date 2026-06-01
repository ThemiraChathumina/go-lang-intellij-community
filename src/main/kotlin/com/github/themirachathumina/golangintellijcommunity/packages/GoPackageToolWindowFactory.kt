package com.github.themirachathumina.golangintellijcommunity.packages

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.io.File

/*
 * Registers the "Go Packages" tool window.
 * isApplicable hides the window entirely for non-Go projects (no go.mod at project root).
 */
class GoPackageToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun shouldBeAvailable(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        return File(basePath, "go.mod").exists()
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = GoPackageToolWindow(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
