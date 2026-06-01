package com.github.themirachathumina.golangintellijcommunity.packages

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.Timer
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/*
 * "Discover" tab — debounced search against pkg.go.dev with a result list and details panel.
 * Tracks which modules are already installed so the details panel can show "✓ installed".
 * onInstalled is called after a successful `go get` so the Project tab can refresh.
 */
class GoDiscoverPanel(
    private val project: Project,
    private val installedPaths: () -> Map<String, String>,
    private val onInstalled: () -> Unit
) : JBPanel<GoDiscoverPanel>(BorderLayout()) {

    private val searchField = SearchTextField()
    private val listModel = DefaultListModel<Any>()
    private val resultList = JBList(listModel)
    private val detailsPanel = GoPackageDetailsPanel(project) { onInstalled() }
    private var debounceTimer: Timer? = null
    private var searchVersion = 0

    init {
        background = UIUtil.getPanelBackground()
        setupContent()
    }

    private fun setupContent() {
        searchField.textEditor.emptyText.text = "Search pkg.go.dev…"
        searchField.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = scheduleSearch()
            override fun removeUpdate(e: DocumentEvent) = scheduleSearch()
            override fun changedUpdate(e: DocumentEvent) = scheduleSearch()
        })

        resultList.cellRenderer = SearchResultRenderer()
        resultList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        resultList.addListSelectionListener {
            if (it.valueIsAdjusting) return@addListSelectionListener
            val v = resultList.selectedValue as? GoPkgDevClient.SearchResult ?: return@addListSelectionListener
            detailsPanel.showSearchResult(v, installedPaths()[v.path])
        }

        val leftPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            add(searchField, BorderLayout.NORTH)
            add(JBScrollPane(resultList), BorderLayout.CENTER)
        }

        val splitter = OnePixelSplitter(false, 0.45f).apply {
            firstComponent = leftPanel
            secondComponent = detailsPanel
        }
        add(splitter, BorderLayout.CENTER)

        showPlaceholder("Search for a package above.")
    }

    fun focusSearch() = searchField.requestFocus()

    private fun scheduleSearch() {
        debounceTimer?.stop()
        debounceTimer = Timer(300) { performSearch(searchField.text.trim()) }.also {
            it.isRepeats = false
            it.start()
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            showPlaceholder("Search for a package above.")
            return
        }

        val version = ++searchVersion
        showPlaceholder("Searching…")

        ApplicationManager.getApplication().executeOnPooledThread {
            val results = GoPkgDevClient.search(query)
            ApplicationManager.getApplication().invokeLater {
                if (searchVersion != version) return@invokeLater
                listModel.clear()
                if (results.isEmpty()) {
                    showPlaceholder("No results for \"$query\".")
                } else {
                    results.forEach { listModel.addElement(it) }
                }
            }
        }
    }

    private fun showPlaceholder(message: String) {
        listModel.clear()
        listModel.addElement(message)
        detailsPanel.showEmpty()
    }

    private class SearchResultRenderer : ListCellRenderer<Any> {
        private val renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>, value: Any?, index: Int, selected: Boolean, focused: Boolean
            ): java.awt.Component {
                super.getListCellRendererComponent(list, value, index, selected, focused)
                border = JBUI.Borders.empty(3, 8)
                return this
            }
        }
        private val placeholder = JBLabel().apply {
            border = JBUI.Borders.empty(4, 8)
            foreground = UIUtil.getContextHelpForeground()
        }

        override fun getListCellRendererComponent(
            list: JList<out Any>, value: Any, index: Int, selected: Boolean, focused: Boolean
        ): java.awt.Component {
            return when (value) {
                is GoPkgDevClient.SearchResult -> {
                    val text = buildString {
                        append(value.path)
                        if (value.synopsis.isNotBlank())
                            append(" — ${value.synopsis.take(70).let { if (value.synopsis.length > 70) "$it…" else it }}")
                    }
                    renderer.getListCellRendererComponent(list, text, index, selected, focused)
                }
                is String -> {
                    placeholder.text = value
                    placeholder
                }
                else -> renderer.getListCellRendererComponent(list, value, index, selected, focused)
            }
        }
    }
}
