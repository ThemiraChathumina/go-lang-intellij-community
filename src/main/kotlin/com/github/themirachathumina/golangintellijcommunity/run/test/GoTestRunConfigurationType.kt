package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.execution.configurations.ConfigurationTypeBase

/*
 * ConfigurationType registration for Go tests, shown as "Go Test" in the Run/Debug 
 * Configurations dialog. Registers GoTestRunConfigurationFactory as its factory.
 */
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
