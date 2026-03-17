package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project

class GoTestRunConfigurationFactory(type: GoTestRunConfigurationType) : ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project) =
        GoTestRunConfiguration(project, this, "Go Test")

    override fun getId() = "GoTestRunConfigurationFactory"
}
