package com.github.themirachathumina.golangintellijcommunity.packages

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLEditorKit

/*
 * Right-hand panel showing details for a selected package.
 * Used by both the Project Dependencies tab (GoModuleItem) and Discover tab (SearchResult).
 * onAction is called after install/update/remove completes successfully so the parent
 * panel can refresh its list.
 */
class GoPackageDetailsPanel(
    private val project: Project,
    private val onAction: () -> Unit
) : JBPanel<GoPackageDetailsPanel>(BorderLayout()) {

    private var currentInstalledItem: GoModuleItem? = null

    private val editorPane = JEditorPane().apply {
        editorKit = HTMLEditorKit()
        isEditable = false
        isOpaque = false
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
        font = UIUtil.getLabelFont()
        addHyperlinkListener { e ->
            if (e.eventType != HyperlinkEvent.EventType.ACTIVATED) return@addHyperlinkListener
            when (e.description) {
                "action://upgrade-latest" -> currentInstalledItem?.let { item ->
                    runGet { goExe, workDir ->
                        GoGetRunner.install(project, goExe, workDir, item.path, "latest") { ok ->
                            if (ok) onAction()
                        }
                    }
                }
                else -> if (e.url != null) BrowserUtil.browse(e.url)
            }
        }
    }

    private val loadingBar = JProgressBar().apply {
        isIndeterminate = true
        isVisible = false
        preferredSize = Dimension(0, 2)
    }

    private val actionPanel = JBPanel<JBPanel<*>>(BorderLayout(4, 0))
    private var detailsFetchVersion = 0
    private var searchFetchVersion = 0

    init {
        background = UIUtil.getPanelBackground()
        border = JBUI.Borders.empty(8)
        add(loadingBar, BorderLayout.NORTH)
        add(JBScrollPane(editorPane).apply { border = JBUI.Borders.empty() }, BorderLayout.CENTER)
        add(actionPanel, BorderLayout.SOUTH)
        showEmpty()
    }

    fun showEmpty() {
        currentInstalledItem = null
        loadingBar.isVisible = false
        editorPane.text = html("<p style='color:gray'>Select a package to see details.</p>")
        actionPanel.removeAll()
        revalidate(); repaint()
    }

    fun showInstalled(item: GoModuleItem) {
        currentInstalledItem = item
        val version = ++detailsFetchVersion

        actionPanel.removeAll()
        var recommendedVersion: String? = null
        val versionCombo = makeVersionCombo(item.version) { recommendedVersion }
        val switchBtn = actionButton("Switch Version") {
            val selected = versionCombo.selectedItem as? String ?: item.version
            runGet { goExe, workDir ->
                GoGetRunner.install(project, goExe, workDir, item.path, selected) { ok ->
                    if (ok) onAction()
                }
            }
        }
        val removeBtn = actionButton("Remove") {
            val confirm = Messages.showYesNoDialog(
                project, "Remove ${item.path} from go.mod?", "Remove Package",
                Messages.getQuestionIcon()
            )
            if (confirm == Messages.YES) {
                runGet { goExe, workDir ->
                    GoGetRunner.remove(project, goExe, workDir, item.path) { ok -> if (ok) onAction() }
                }
            }
        }
        val btnPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0)).apply {
            isOpaque = false
            add(switchBtn)
            add(removeBtn)
        }
        actionPanel.add(versionCombo, BorderLayout.CENTER)
        actionPanel.add(btnPanel, BorderLayout.EAST)

        val cachedDetails = GoPkgDevClient.getCachedDetails(item.path)
        val cachedVersions = GoPkgDevClient.getCachedVersions(item.path)

        if (cachedDetails != null && cachedVersions != null) {
            val upgradeVersion = cachedVersions.firstOrNull()?.takeIf { it != item.version }
            renderInstalled(item, synopsis = cachedDetails.synopsis, license = cachedDetails.license, upgradeVersion = upgradeVersion)
            recommendedVersion = populateCombo(versionCombo, cachedVersions, item.version)
        } else {
            loadingBar.isVisible = true
            renderInstalled(item, synopsis = "Loading…", license = "…")
            ApplicationManager.getApplication().executeOnPooledThread {
                val details = GoPkgDevClient.fetchDetails(item.path)
                val versions = GoPkgDevClient.fetchVersions(item.path)
                ApplicationManager.getApplication().invokeLater {
                    if (detailsFetchVersion != version) return@invokeLater
                    loadingBar.isVisible = false
                    val upgradeVersion = versions.firstOrNull()?.takeIf { it != item.version }
                    renderInstalled(item, synopsis = details.synopsis, license = details.license, upgradeVersion = upgradeVersion)
                    recommendedVersion = populateCombo(versionCombo, versions, item.version)
                }
            }
        }
        revalidate(); repaint()
    }

    private fun renderInstalled(item: GoModuleItem, synopsis: String, license: String, upgradeVersion: String? = null) {
        val effectiveUpgrade = upgradeVersion ?: item.latestVersion?.takeIf { item.hasUpdate }
        val upgradeBanner = if (effectiveUpgrade != null)
            "<div style='margin-bottom:6px;padding:4px 6px;background:#5a3e00;border-radius:4px'>" +
            "&#x2B06; Update available: <b>$effectiveUpgrade</b> &nbsp;" +
            "<a href='action://upgrade-latest' style='color:orange'>Upgrade now</a></div>"
        else ""

        val synopsisLine = if (synopsis.isNotBlank()) "<br/><br/>$synopsis" else ""
        val licenseLine  = if (license.isNotBlank()) "<br/><br/><b>License:</b> $license"
                           else "<br/><br/><b>License:</b> —"

        editorPane.text = html("""
            $upgradeBanner
            <b style='font-size:13px'>${item.path}</b><hr/>
            <b>Installed:</b> ${item.version}
            $synopsisLine
            $licenseLine
            <br/><br/>
            <a href='${item.pkgDevUrl}'>pkg.go.dev ↗</a>
        """.trimIndent())
        revalidate(); repaint()
    }

    fun showSearchResult(result: GoPkgDevClient.SearchResult, installedVersion: String?) {
        currentInstalledItem = null
        val fetchVersion = ++searchFetchVersion

        val installedLine = if (installedVersion != null)
            "<br/><b>Installed:</b> $installedVersion <span style='color:green'>✓</span>"
        else ""

        val licenseLine = if (result.licenses.isNotEmpty())
            "<br/><b>License:</b> ${result.licenses.joinToString()}"
        else ""

        editorPane.text = html("""
            <b style='font-size:13px'>${result.path}</b><hr/>
            <b>Latest:</b> ${result.version}$installedLine
            <br/><br/>
            ${result.synopsis}
            $licenseLine
            <br/><br/>
            <a href='${result.pkgDevUrl}'>pkg.go.dev ↗</a>
        """.trimIndent())

        actionPanel.removeAll()
        var recommendedVersion: String? = null
        val versionCombo = makeVersionCombo(result.version) { recommendedVersion }
        val installBtn = actionButton(if (installedVersion == null) "Install" else "Switch Version") {
            val selected = versionCombo.selectedItem as? String ?: result.version
            runGet { goExe, workDir ->
                GoGetRunner.install(project, goExe, workDir, result.path, selected) { ok ->
                    if (ok) onAction()
                }
            }
        }
        actionPanel.add(versionCombo, BorderLayout.CENTER)
        actionPanel.add(installBtn, BorderLayout.EAST)

        val cachedVersions = GoPkgDevClient.getCachedVersions(result.path)
        if (cachedVersions != null) {
            recommendedVersion = populateCombo(versionCombo, cachedVersions, installedVersion ?: result.version)
        } else {
            loadingBar.isVisible = true
            ApplicationManager.getApplication().executeOnPooledThread {
                val versions = GoPkgDevClient.fetchVersions(result.path)
                ApplicationManager.getApplication().invokeLater {
                    if (searchFetchVersion != fetchVersion) return@invokeLater
                    loadingBar.isVisible = false
                    recommendedVersion = populateCombo(versionCombo, versions, installedVersion ?: result.version)
                }
            }
        }
        revalidate(); repaint()
    }

    private fun makeVersionCombo(placeholder: String, recommended: () -> String?): JComboBox<String> =
        JComboBox(arrayOf(placeholder)).apply {
            isEnabled = false
            toolTipText = "Loading available versions…"
            renderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>, value: Any?, index: Int, selected: Boolean, hasFocus: Boolean
                ): java.awt.Component {
                    val display = if (value != null && value == recommended()) "$value (recommended)" else value
                    return super.getListCellRendererComponent(list, display, index, selected, hasFocus)
                }
            }
        }

    private fun populateCombo(combo: JComboBox<String>, versions: List<String>, preferredSelection: String): String? {
        combo.removeAllItems()
        val recommended: String?
        if (versions.isNotEmpty()) {
            recommended = versions[0]
            versions.forEach { combo.addItem(it) }
        } else {
            recommended = preferredSelection
            combo.addItem(preferredSelection)
        }
        combo.selectedItem = preferredSelection.takeIf { versions.contains(it) } ?: combo.getItemAt(0)
        combo.isEnabled = true
        combo.toolTipText = null
        return recommended
    }

    private fun actionButton(text: String, action: () -> Unit): JButton =
        JButton(text).apply { addActionListener { action() } }

    private fun runGet(block: (goExe: String, workDir: String) -> Unit) {
        val sdkPath = com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
            .getInstance(project).state.goSdkPath
        val goExe = com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkLocator
            .getGoExecutablePath(sdkPath) ?: return
        val workDir = project.basePath ?: return
        block(goExe, workDir)
    }

    private fun html(body: String): String {
        val bg = String.format("#%06x", UIUtil.getPanelBackground().rgb and 0xFFFFFF)
        val fg = String.format("#%06x", UIUtil.getLabelForeground().rgb and 0xFFFFFF)
        return """<html><body style='background:$bg;color:$fg;font-family:sans-serif;font-size:12px;margin:4px'>
            $body
        </body></html>""".trimIndent()
    }
}
