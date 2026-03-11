package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element

class GoRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    LocatableConfigurationBase<RunProfileState>(project, factory, name) {

    var runKind: GoRunKind = GoRunKind.FILE
    var targetPath: String = ""
    var workingDirectory: String = project.basePath ?: ""
    var programArguments: String = ""
    var goToolArguments: String = ""
    var envVariables: MutableMap<String, String> = mutableMapOf()

    // 1. Returns the UI component for the Edit Run Configuration dialog
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return GoRunSettingsEditor(project)
    }

    // 2. Returns the execution engine when the user clicks "Play"
    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return GoRunProfileState(environment, this)
    }

    // 3. Save settings to disk
    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.setAttribute("runKind", runKind.name)
        element.setAttribute("targetPath", targetPath)
        element.setAttribute("workingDirectory", workingDirectory)
        element.setAttribute("programArguments", programArguments)
        element.setAttribute("goToolArguments", goToolArguments)

        val envsElement = Element("envs")
        envVariables.forEach { (k, v) ->
            val env = Element("env")
            env.setAttribute("name", k)
            env.setAttribute("value", v)
            envsElement.addContent(env)
        }
        element.addContent(envsElement)
    }

    // 4. Read settings from disk
    override fun readExternal(element: Element) {
        super.readExternal(element)
        runKind = GoRunKind.valueOf(element.getAttributeValue("runKind") ?: GoRunKind.FILE.name)
        targetPath = element.getAttributeValue("targetPath") ?: ""
        workingDirectory = element.getAttributeValue("workingDirectory") ?: ""
        programArguments = element.getAttributeValue("programArguments") ?: ""
        goToolArguments = element.getAttributeValue("goToolArguments") ?: ""

        val envsElement = element.getChild("envs")
        envVariables.clear()
        envsElement?.getChildren("env")?.forEach {
            envVariables[it.getAttributeValue("name")] = it.getAttributeValue("value")
        }
    }
}