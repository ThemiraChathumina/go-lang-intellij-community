package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.startup.ProjectActivity
import com.redhat.devtools.lsp4ij.LanguageServerManager
import java.io.File

class GoSdkAutoDetector : ProjectActivity {
    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return

        // 1. Safety Check: Make sure we don't accidentally hijack a real Java project!
        // We scan up to 2 directories deep to see if there are any .go files or a go.mod
        val rootDir = File(basePath)
        val isGoProject = rootDir.walkTopDown().maxDepth(2).any {
            it.name == "go.mod" || it.name.endsWith(".go")
        }

        if (!isGoProject) return

        // 2. Check the currently assigned SDK
        val currentSdk = ProjectRootManager.getInstance(project).projectSdk
        if (currentSdk != null && currentSdk.sdkType is GoSdkType) {
            // It is already a Go SDK! Just start the language server and we are done.
            ApplicationManager.getApplication().invokeLater {
                LanguageServerManager.getInstance(project).start("gopls")
            }
            return
        }

        // 3. Search for a valid Go SDK on the system
        val suggestedPaths = GoSdkUtils.suggestSdkHomePaths()
        if (suggestedPaths.isEmpty()) return

        val bestPath = suggestedPaths.first()
        GoSdkUtils.retrieveGoVersion(bestPath) // Prime the cache

        val goSdkType = GoSdkType.getInstance()

        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().runWriteAction {
                val existingSdk = ProjectJdkTable.getInstance().allJdks.find {
                    it.sdkType == goSdkType && it.homePath == bestPath
                }

                val sdk = existingSdk ?: SdkConfigurationUtil.createAndAddSDK(bestPath, goSdkType)

                if (sdk != null) {
                    // A. Force set the Project SDK (Overrides the Java JDK)
                    ProjectRootManager.getInstance(project).projectSdk = sdk
                    SdkConfigurationUtil.setDirectoryProjectSdk(project, sdk)

                    // B. THE CRITICAL FIX: Force all IDE Modules to inherit the Project SDK
                    // This strips the Java JDK off the individual modules so they use Go.
                    val modules = ModuleManager.getInstance(project).modules
                    for (module in modules) {
                        ModuleRootModificationUtil.setSdkInherited(module)
                    }

                    // Start the language server now that the environment is fully Go
                    LanguageServerManager.getInstance(project).start("gopls")
                }
            }
        }
    }
}