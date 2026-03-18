package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageRunner
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState

/**
 * Stores coverage settings for a Go test run configuration.
 *
 * Mirrors [io.flutter.run.coverage.FlutterCoverageEnabledConfiguration]: after
 * setting the runner and creating the coverage file path we register this
 * configuration as a suite with [CoverageDataManager] and mark it as the
 * current suite.  That registration is what causes the Coverage tool window to
 * open automatically when a "Run with Coverage" session finishes.
 */
class GoCoverageEnabledConfiguration(config: RunConfigurationBase<*>) :
    CoverageEnabledConfiguration(config) {

    init {
        setCoverageRunner(CoverageRunner.getInstance(GoCoverageRunner::class.java))
        createCoverageFile()
        ApplicationManager.getApplication().invokeLater({
            setCurrentCoverageSuite(
                CoverageDataManager.getInstance(configuration.project).addCoverageSuite(this)
            )
        }, ModalityState.any())
    }
}
