package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

/*
 * Factory that creates and manages the lifecycle of the GoSdkStatusBarWidget.
 * Ensures the widget is available and enabled in the IDE status bar.
 */
class GoSdkStatusBarWidgetFactory : StatusBarWidgetFactory {
    private val LOG = Logger.getInstance(GoSdkStatusBarWidgetFactory::class.java)

    override fun getId(): String = "GoSdkStatusBarWidget"

    override fun getDisplayName(): String = "Go SDK Version"

    override fun isAvailable(project: Project): Boolean {
        return true
    }

    override fun createWidget(project: Project): StatusBarWidget {
        LOG.debug("${GoConstants.Log.PREFIX} Creating Go SDK widget for ${project.name}")
        return GoSdkStatusBarWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        LOG.debug("${GoConstants.Log.PREFIX} Disposing Go SDK widget")
        Disposer.dispose(widget)
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}