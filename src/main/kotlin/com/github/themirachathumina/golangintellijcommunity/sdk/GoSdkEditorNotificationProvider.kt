package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

class GoSdkEditorNotificationProvider : EditorNotificationProvider, DumbAware {
    private val LOG = Logger.getInstance(GoSdkEditorNotificationProvider::class.java)

    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.extension != GoConstants.FileExtensions.GO_EXT) return null

        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        if (sdkPath.isNotEmpty() && GoSdkUtils.isValidSdkHome(sdkPath)) return null

        LOG.info("${GoConstants.Log.PREFIX} Showing SDK configuration notification for ${file.name}")

        return Function { fileEditor: FileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning)
            panel.text = "Go SDK is not configured for this project"
            panel.createActionLabel("Configure Go SDK...") {
                LOG.info("${GoConstants.Log.PREFIX} User clicked 'Configure Go SDK' in notification")
                // Instruct user to use status bar widget
                com.intellij.openapi.ui.Messages.showInfoMessage(project, "Please click on 'No Go SDK' in the status bar at the bottom to configure or download a Go SDK.", "Configure Go SDK")
            }
            panel
        }
    }
}
