package com.github.themirachathumina.golangintellijcommunity.wizard

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import javax.swing.Icon

class GoNewProjectWizard : GeneratorNewProjectWizard {
    override val id: String = "Go"
    override val name: String = "Go"
    override val icon: Icon = GoIcons.FILE

    override fun createStep(context: WizardContext): NewProjectWizardStep {
        return RootNewProjectWizardStep(context)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::GitNewProjectWizardStep)
            .nextStep(::GoSettingsStep)
    }
}
