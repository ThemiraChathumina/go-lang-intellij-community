package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.execution.configurations.ConfigurationTypeBase

class GoTestRunConfigurationType : ConfigurationTypeBase(
    "GoTestRunConfiguration",
    "Go Test",
    "Run Go tests",
    GoIcons.FILE
) {
    init {
        addFactory(GoTestRunConfigurationFactory(this))
    }
}
