package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.execution.configurations.ConfigurationTypeBase

class GoRunConfigurationType : ConfigurationTypeBase(
    "GoRunConfiguration",
    "Go",
    "Run a Go application",
    GoIcons.FILE
) {
    init {
        addFactory(GoRunConfigurationFactory(this))
    }
}