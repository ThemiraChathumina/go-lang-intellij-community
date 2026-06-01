package com.github.themirachathumina.golangintellijcommunity.packages

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout

/*
 * Root panel for the "Go Packages" tool window.
 * Hosts the Project Dependencies tab and the Discover tab and wires them together
 * so that installing a package from Discover triggers a reload in Project Dependencies.
 */
class GoPackageToolWindow(project: Project) : JBPanel<GoPackageToolWindow>(BorderLayout()) {

    private val depsPanel = GoProjectDepsPanel(project)

    private val discoverPanel = GoDiscoverPanel(
        project = project,
        installedPaths = { depsPanel.installedPaths() },
        onInstalled = { depsPanel.reload(checkUpdates = false) }
    )

    private val tabs = JBTabbedPane().apply {
        addTab("Project Dependencies", depsPanel)
        addTab("Discover", discoverPanel)
    }

    init {
        background = UIUtil.getPanelBackground()
        add(tabs, BorderLayout.CENTER)
    }
}
