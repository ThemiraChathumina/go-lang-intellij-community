package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkUtils
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XMap
import org.jdom.Element

/**
 * Run configuration for Go tests.
 */
class GoTestRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    LocatableConfigurationBase<RunProfileState>(project, factory, name) {

    private var state = GoTestRunConfigurationState()

    var scope: GoTestScope
        get() = state.scope
        set(value) { state.scope = value }

    var targetPath: String
        get() = state.targetPath
        set(value) { state.targetPath = FileUtil.toSystemIndependentName(value) }

    var workingDirectory: String
        get() = state.workingDirectory
        set(value) { state.workingDirectory = FileUtil.toSystemIndependentName(value) }

    var buildTags: String
        get() = state.buildTags
        set(value) { state.buildTags = value }

    var envVariables: MutableMap<String, String>
        get() = state.envVariables
        set(value) { state.envVariables = value }

    var isShort: Boolean
        get() = state.isShort
        set(value) { state.isShort = value }

    var isRace: Boolean
        get() = state.isRace
        set(value) { state.isRace = value }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        // We'll implement this editor later if needed, returning a dummy for now.
        // Actually, for Phase 2, a dummy might be fine but it needs to be valid.
        return GoTestSettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return GoTestCommandLineState(environment, this)
    }

    override fun checkConfiguration() {
        if (targetPath.isBlank()) {
            throw RuntimeConfigurationError("Target path is not specified")
        }

        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        if (sdkPath.isBlank() || !GoSdkUtils.isValidSdkHome(sdkPath)) {
            throw RuntimeConfigurationError("Go SDK is not configured for this project")
        }
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        XmlSerializer.serializeInto(state, element)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        XmlSerializer.deserializeInto(state, element)
    }

    override fun clone(): RunConfiguration {
        val cloned = super.clone() as GoTestRunConfiguration
        cloned.state = this.state.copy()
        return cloned
    }

    private class GoTestRunConfigurationState {
        @Attribute("scope")
        var scope: GoTestScope = GoTestScope.DIRECTORY
        @Attribute("targetPath")
        var targetPath: String = ""
        @Attribute("workingDirectory")
        var workingDirectory: String = ""
        @Attribute("buildTags")
        var buildTags: String = ""

        @Tag("envs")
        @XMap(entryTagName = "env", keyAttributeName = "name", valueAttributeName = "value")
        var envVariables: MutableMap<String, String> = mutableMapOf()

        @Attribute("isShort")
        var isShort: Boolean = false
        @Attribute("isRace")
        var isRace: Boolean = false

        fun copy(): GoTestRunConfigurationState {
            val copy = GoTestRunConfigurationState()
            copy.scope = this.scope
            copy.targetPath = this.targetPath
            copy.workingDirectory = this.workingDirectory
            copy.buildTags = this.buildTags
            copy.envVariables = HashMap(this.envVariables)
            copy.isShort = this.isShort
            copy.isRace = this.isRace
            return copy
        }
    }

    init {
        workingDirectory = project.basePath ?: ""
    }
}
