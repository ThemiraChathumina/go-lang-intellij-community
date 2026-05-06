package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

/*
 * Status bar widget that shows a "Compiling…" message during a Go build and provides a cancel button.
 * Install and remove it via the companion's install/remove helpers.
 */
class GoCompilationStatusWidget(
    private val message: String,
    private val onCancel: () -> Unit
) : StatusBarWidget, StatusBarWidget.TextPresentation {

    companion object {
        const val ID = "GoCompilationStatus"

        fun install(project: Project, message: String, onCancel: () -> Unit): GoCompilationStatusWidget {
            val widget = GoCompilationStatusWidget(message, onCancel)
            ApplicationManager.getApplication().invokeLater {
                val statusBar = WindowManager.getInstance().getStatusBar(project) ?: return@invokeLater
                statusBar.addWidget(widget, project)
                statusBar.updateWidget(ID)
            }
            return widget
        }

        fun remove(project: Project) {
            ApplicationManager.getApplication().invokeLater {
                WindowManager.getInstance().getStatusBar(project)?.removeWidget(ID)
            }
        }
    }

    override fun ID(): String = ID
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this
    override fun install(statusBar: StatusBar) {}
    override fun dispose() {}

    override fun getText(): String = "$message  [Cancel]"
    override fun getTooltipText(): String = "Click to cancel Go compilation"
    override fun getAlignment(): Float = 0f
    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer { onCancel() }
}
