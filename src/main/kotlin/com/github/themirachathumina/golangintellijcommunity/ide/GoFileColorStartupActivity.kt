package com.github.themirachathumina.golangintellijcommunity.ide

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.ui.FileColorManager

/**
 * Sets default file colors for Go-specific scopes on first project open.
 * [FileColorManager.addColor] is a no-op when the scope already has a color,
 * so this is safe to run on every startup without overriding user customisations.
 */
class GoFileColorStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val manager = FileColorManager.getInstance(project)
        if (manager.getScopeColor(GoFileScopesProvider.TEST_FILES_SCOPE) == null) {
            manager.addScopeColor(GoFileScopesProvider.TEST_FILES_SCOPE, "Green", false)
        }
        if (manager.getScopeColor(GoFileScopesProvider.SUM_FILE_SCOPE) == null) {
            manager.addScopeColor(GoFileScopesProvider.SUM_FILE_SCOPE, "Yellow", false)
        }
    }
}
