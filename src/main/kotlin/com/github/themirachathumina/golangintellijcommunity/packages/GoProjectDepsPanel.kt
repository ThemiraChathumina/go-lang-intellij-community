package com.github.themirachathumina.golangintellijcommunity.packages

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkLocator
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/*
 * "Project Dependencies" tab — shows all modules from go.mod in a split-pane layout.
 * Left: filterable grouped list (Direct / Indirect). Right: GoPackageDetailsPanel.
 * Refresh reloads from `go list -m -json all`; Check for Updates also passes -u.
 */
class GoProjectDepsPanel(private val project: Project) : JBPanel<GoProjectDepsPanel>(BorderLayout()) {

    private val listModel = DefaultListModel<Any>()
    private val packageList = JBList(listModel)
    private val filterField = SearchTextField()
    private val detailsPanel = GoPackageDetailsPanel(project) { reload(checkUpdates = false) }
    private var allModules: List<GoModuleItem> = emptyList()

    init {
        background = UIUtil.getPanelBackground()
        setupToolbar()
        setupContent()
        reload(checkUpdates = false)
    }

    private fun setupToolbar() {
        val group = DefaultActionGroup()
        group.add(object : AnAction("Refresh", "Reload from go.mod", AllIcons.Actions.Refresh) {
            override fun getActionUpdateThread() = ActionUpdateThread.BGT
            override fun actionPerformed(e: AnActionEvent) = reload(checkUpdates = false)
        })
        group.add(object : AnAction("Check for Updates", "Check latest versions online", AllIcons.Actions.CheckOut) {
            override fun getActionUpdateThread() = ActionUpdateThread.BGT
            override fun actionPerformed(e: AnActionEvent) = reload(checkUpdates = true)
        })
        group.addSeparator()
        group.add(object : AnAction("Update All", "Run go get -u ./...", AllIcons.Actions.Upload) {
            override fun getActionUpdateThread() = ActionUpdateThread.BGT
            override fun actionPerformed(e: AnActionEvent) {
                val confirm = Messages.showYesNoDialog(
                    project, "Update all dependencies to their latest versions?",
                    "Update All", Messages.getQuestionIcon()
                )
                if (confirm != Messages.YES) return
                val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
                val goExe = GoSdkLocator.getGoExecutablePath(sdkPath) ?: return
                val workDir = project.basePath ?: return
                GoGetRunner.updateAll(project, goExe, workDir) { ok -> if (ok) reload(false) }
            }
        })
        val toolbar = ActionManager.getInstance()
            .createActionToolbar("GoProjectDeps", group, true)
        toolbar.targetComponent = this
        add(toolbar.component, BorderLayout.NORTH)
    }

    private fun setupContent() {
        packageList.cellRenderer = GoModuleListRenderer()
        packageList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        packageList.addListSelectionListener {
            if (it.valueIsAdjusting) return@addListSelectionListener
            when (val v = packageList.selectedValue) {
                is GoModuleItem -> detailsPanel.showInstalled(v)
                else -> detailsPanel.showEmpty()
            }
        }

        filterField.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = applyFilter()
            override fun removeUpdate(e: DocumentEvent) = applyFilter()
            override fun changedUpdate(e: DocumentEvent) = applyFilter()
        })

        val leftPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            add(filterField, BorderLayout.NORTH)
            add(JBScrollPane(packageList), BorderLayout.CENTER)
        }

        val splitter = OnePixelSplitter(false, 0.45f).apply {
            firstComponent = leftPanel
            secondComponent = detailsPanel
        }
        add(splitter, BorderLayout.CENTER)
    }

    fun reload(checkUpdates: Boolean) {
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        val goExe = GoSdkLocator.getGoExecutablePath(sdkPath)
        val workDir = project.basePath

        if (goExe == null || workDir == null) {
            showSdkNotConfigured()
            return
        }

        object : Task.Backgroundable(project, "Loading Go Dependencies", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val result = try {
                    GoModReader.read(goExe, workDir, checkUpdates)
                } catch (_: Exception) {
                    GoModReader.Result(emptyList(), "")
                }
                ApplicationManager.getApplication().invokeLater {
                    allModules = result.modules
                    applyFilter()
                }
            }
        }.queue()
    }

    fun installedPaths(): Map<String, String> = allModules.associate { it.path to it.version }

    private fun applyFilter() {
        val query = filterField.text.trim()
        val filtered = if (query.isEmpty()) allModules
        else allModules.filter { it.path.contains(query, ignoreCase = true) }

        listModel.clear()
        val direct = filtered.filter { !it.indirect }
        val indirect = filtered.filter { it.indirect }

        if (direct.isNotEmpty()) {
            listModel.addElement("DIRECT (${direct.size})")
            direct.forEach { listModel.addElement(it) }
        }
        if (indirect.isNotEmpty()) {
            listModel.addElement("INDIRECT (${indirect.size})")
            indirect.forEach { listModel.addElement(it) }
        }
        if (filtered.isEmpty()) {
            listModel.addElement("No packages found")
        }
    }

    private fun showSdkNotConfigured() {
        listModel.clear()
        listModel.addElement("Go SDK not configured")
    }

    private class GoModuleListRenderer : ListCellRenderer<Any> {
        private val itemRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>, value: Any?, index: Int, selected: Boolean, focused: Boolean
            ): Component {
                super.getListCellRendererComponent(list, value, index, selected, focused)
                border = JBUI.Borders.empty(2, 8)
                return this
            }
        }
        private val headerLabel = JBLabel().apply {
            border = JBUI.Borders.empty(6, 6, 2, 6)
            foreground = UIUtil.getContextHelpForeground()
            font = UIUtil.getLabelFont().deriveFont(java.awt.Font.BOLD)
        }

        override fun getListCellRendererComponent(
            list: JList<out Any>, value: Any, index: Int, selected: Boolean, focused: Boolean
        ): Component {
            return when (value) {
                is GoModuleItem -> {
                    val label = StringBuilder(value.path)
                    label.append("  ${value.version}")
                    if (value.hasUpdate) label.append("  ↑")
                    itemRenderer.getListCellRendererComponent(list, label.toString(), index, selected, focused)
                }
                is String -> {
                    headerLabel.text = value
                    headerLabel
                }
                else -> itemRenderer.getListCellRendererComponent(list, value, index, selected, focused)
            }
        }
    }
}
