package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.ui.components.JBLabel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class GoSdkStatusBarWidget(private val project: Project) : CustomStatusBarWidget {
    private val LOG = Logger.getInstance(GoSdkStatusBarWidget::class.java)
    private val label = JBLabel()

    init {
        updateLabel()
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                LOG.info("[Go-Lang-Community] Go SDK widget clicked, updating label")
                updateLabel()
            }
        })
    }

    private fun updateLabel() {
        val sdkService = project.getService(GoSdkService::class.java)
        val sdk = sdkService.getSdk()
        if (sdk != null) {
            label.icon = GoIcons.FILE
            label.text = sdk.versionString ?: "Go SDK"
            label.toolTipText = "Current Go SDK: ${sdk.homePath}"
            LOG.debug("[Go-Lang-Community] Widget label updated to SDK: ${label.text}")
        } else {
            // Fallback for auto-detected but not yet assigned
            val bestPath = GoSdkUtils.suggestSdkHomePaths().firstOrNull()
            if (bestPath != null) {
                label.icon = GoIcons.FILE
                label.text = GoSdkUtils.retrieveGoVersion(bestPath) ?: "Go SDK"
                LOG.debug("[Go-Lang-Community] Widget label updated to suggested SDK: ${label.text}")
            } else {
                LOG.debug("[Go-Lang-Community] Widget label: no SDK found")
            }
        }
    }

    override fun getComponent(): JComponent = label

    override fun ID(): String = "GoSdkStatusBarWidget"

    override fun install(statusBar: StatusBar) {}

    override fun dispose() {}
}
