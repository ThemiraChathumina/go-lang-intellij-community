package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.ui.components.JBLabel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

class GoSdkStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "GoSdkStatusBarWidget"

    override fun getDisplayName(): String = "Go SDK Version"

    override fun isAvailable(project: Project): Boolean {
        return project.getService(GoSdkService::class.java).isSdkValid()
    }

    override fun createWidget(project: Project): StatusBarWidget {
        return GoSdkStatusBarWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        Disposer.dispose(widget)
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

class GoSdkStatusBarWidget(private val project: Project) : CustomStatusBarWidget {
    private val label = JBLabel()

    init {
        updateLabel()
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                // Show SDK version in a tooltip or popup if needed, 
                // but standard click behavior can just refresh or open settings
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
        } else {
            // Fallback for auto-detected but not yet assigned
            val bestPath = GoSdkUtils.suggestSdkHomePaths().firstOrNull()
            if (bestPath != null) {
                label.icon = GoIcons.FILE
                label.text = GoSdkUtils.retrieveGoVersion(bestPath) ?: "Go SDK"
            }
        }
    }

    override fun getComponent(): JComponent = label

    override fun ID(): String = "GoSdkStatusBarWidget"

    override fun install(statusBar: StatusBar) {}

    override fun dispose() {}
}
