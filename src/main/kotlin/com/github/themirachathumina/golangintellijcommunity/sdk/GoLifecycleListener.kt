package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.Project

/*
 * Listens for IDE lifecycle events to trigger initialization tasks. 
 * Specifically, it initiates an initial scan of Go SDKs when the application frame is created.
 */
class GoLifecycleListener : AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: List<String>) {
        // Trigger initial SDK scan as soon as the IDE window is created.
        // This populates the version cache in the background before the user opens any UI.
        GoGlobalSdkRegistry.getInstance().getSdkPaths()
    }
}
