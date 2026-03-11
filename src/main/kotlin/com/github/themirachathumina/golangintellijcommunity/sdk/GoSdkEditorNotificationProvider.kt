package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

class GoSdkEditorNotificationProvider : EditorNotificationProvider, DumbAware {
    private val LOG = Logger.getInstance(GoSdkEditorNotificationProvider::class.java)

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.extension != "go") return null

        val sdkService = project.getService(GoSdkService::class.java)
        if (sdkService.isSdkValid()) return null

        LOG.info("[Go-Lang-Community] Showing SDK configuration notification for ${file.name}")

        return Function { fileEditor: FileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning)
            panel.text = "Go SDK is not configured"
            panel.createActionLabel("Setup SDK") {
                LOG.info("[Go-Lang-Community] User clicked 'Setup SDK' in notification")
                ProjectSettingsService.getInstance(project).openProjectSettings()
            }
            panel
        }
    }
}
