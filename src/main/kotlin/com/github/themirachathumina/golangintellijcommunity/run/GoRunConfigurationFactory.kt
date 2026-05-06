package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project

/*
 * Factory that creates new GoRunConfiguration template instances for the Go run configuration type.
 */
class GoRunConfigurationFactory(type: GoRunConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project) =
        GoRunConfiguration(project, this, "Go Build")

    override fun getId() = "GoRunConfigurationFactory"
}