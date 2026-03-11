package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil

class GoGlobalSdkDetector : AppLifecycleListener {
    private val LOG = Logger.getInstance(GoGlobalSdkDetector::class.java)

    override fun appFrameCreated(commandLineArgs: List<String>) {
        // 1. THE CHECK: Are we actually on the Welcome Screen?
        val openProjects = ProjectManager.getInstance().openProjects
        if (openProjects.isNotEmpty()) {
            LOG.info("[Go-Lang-Community] IDE launched directly into a project. Skipping Welcome Screen SDK detection.")
            return // Bail out early to save CPU!
        }

        // 2. We are idling on the Welcome Screen, safe to detect!
        ApplicationManager.getApplication().executeOnPooledThread {
            LOG.info("[Go-Lang-Community] Welcome Screen active: Checking for global Go SDK...")

            val goSdkType = GoSdkType.getInstance()
            val existingSdks = ProjectJdkTable.getInstance().getSdksOfType(goSdkType)

            if (existingSdks.isEmpty()) {
                LOG.info("[Go-Lang-Community] No global Go SDK found. Auto-detecting in background...")
                val suggestedPath = GoSdkUtils.suggestSdkHomePaths().firstOrNull()

                if (suggestedPath != null) {
                    // Prime the cache safely off the UI thread
                    GoSdkUtils.retrieveGoVersion(suggestedPath)

                    // Jump back to the EDT to save it to the IDE's global settings
                    ApplicationManager.getApplication().invokeLater {
                        ApplicationManager.getApplication().runWriteAction {
                            // Double-check just in case the user was incredibly fast!
                            if (ProjectJdkTable.getInstance().getSdksOfType(goSdkType).isEmpty()) {
                                SdkConfigurationUtil.createAndAddSDK(suggestedPath, goSdkType)
                                LOG.info("[Go-Lang-Community] Global Go SDK successfully added from Welcome Screen!")
                            }
                        }
                    }
                } else {
                    LOG.warn("[Go-Lang-Community] Global detection failed: No Go SDK found on system.")
                }
            } else {
                LOG.info("[Go-Lang-Community] Global Go SDK already exists. Skipping detection.")
            }
        }
    }
}