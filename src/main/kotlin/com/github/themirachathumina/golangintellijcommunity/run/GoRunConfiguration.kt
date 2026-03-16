package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.sdk.GoGlobalSdkRegistry
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

enum class GoBuildMode { DEFAULT, DEBUG, RELEASE }

class GoRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    LocatableConfigurationBase<RunProfileState>(project, factory, name) {

    private var state = GoRunConfigurationState()

    var runKind: GoRunKind
        get() = state.runKind
        set(value) { state.runKind = value }

    var targetPath: String
        get() = state.targetPath
        set(value) { state.targetPath = FileUtil.toSystemIndependentName(value) }

    var workingDirectory: String
        get() = state.workingDirectory
        set(value) { state.workingDirectory = FileUtil.toSystemIndependentName(value) }

    var programArguments: String
        get() = state.programArguments
        set(value) { state.programArguments = value }

    var goToolArguments: String
        get() = state.goToolArguments
        set(value) { state.goToolArguments = value }

    var envVariables: MutableMap<String, String>
        get() = state.envVariables
        set(value) { state.envVariables = value }

    var enableRace: Boolean
        get() = state.enableRace
        set(value) { state.enableRace = value }

    var buildTags: String
        get() = state.buildTags
        set(value) { state.buildTags = value }

    var buildMode: GoBuildMode
        get() = state.buildMode
        set(value) { state.buildMode = value }

    var goOS: String
        get() = state.goOS
        set(value) { state.goOS = value }

    var goArch: String
        get() = state.goArch
        set(value) { state.goArch = value }

    var cgoEnabled: Boolean
        get() = state.cgoEnabled
        set(value) { state.cgoEnabled = value }

    var outputPath: String
        get() = state.outputPath
        set(value) { state.outputPath = FileUtil.toSystemIndependentName(value) }

    var goProxy: String
        get() = state.goProxy
        set(value) { state.goProxy = value }

    var stdinFilePath: String
        get() = state.stdinFilePath
        set(value) { state.stdinFilePath = FileUtil.toSystemIndependentName(value) }

    var runInExternalTerminal: Boolean
        get() = state.runInExternalTerminal
        set(value) { state.runInExternalTerminal = value }

    var timeoutSeconds: Int
        get() = state.timeoutSeconds
        set(value) { state.timeoutSeconds = value }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return GoRunSettingsEditor(project)
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

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return GoRunProfileState(environment, this)
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
        val cloned = super.clone() as GoRunConfiguration
        cloned.state = this.state.copy()
        return cloned
    }

    private class GoRunConfigurationState {
        @Attribute("runKind")
        var runKind: GoRunKind = GoRunKind.FILE
        @Attribute("targetPath")
        var targetPath: String = ""
        @Attribute("workingDirectory")
        var workingDirectory: String = ""
        @Attribute("programArguments")
        var programArguments: String = ""
        @Attribute("goToolArguments")
        var goToolArguments: String = ""

        @Tag("envs")
        @XMap(entryTagName = "env", keyAttributeName = "name", valueAttributeName = "value")
        var envVariables: MutableMap<String, String> = mutableMapOf()

        @Attribute("enableRace")
        var enableRace: Boolean = false
        @Attribute("buildTags")
        var buildTags: String = ""
        @Attribute("buildMode")
        var buildMode: GoBuildMode = GoBuildMode.DEFAULT
        @Attribute("goOS")
        var goOS: String = ""
        @Attribute("goArch")
        var goArch: String = ""
        @Attribute("cgoEnabled")
        var cgoEnabled: Boolean = false
        @Attribute("outputPath")
        var outputPath: String = ""
        @Attribute("goProxy")
        var goProxy: String = ""
        @Attribute("stdinFilePath")
        var stdinFilePath: String = ""
        @Attribute("runInExternalTerminal")
        var runInExternalTerminal: Boolean = false
        @Attribute("timeoutSeconds")
        var timeoutSeconds: Int = 0

        fun copy(): GoRunConfigurationState {
            val copy = GoRunConfigurationState()
            copy.runKind = this.runKind
            copy.targetPath = this.targetPath
            copy.workingDirectory = this.workingDirectory
            copy.programArguments = this.programArguments
            copy.goToolArguments = this.goToolArguments
            copy.envVariables = HashMap(this.envVariables)
            copy.enableRace = this.enableRace
            copy.buildTags = this.buildTags
            copy.buildMode = this.buildMode
            copy.goOS = this.goOS
            copy.goArch = this.goArch
            copy.cgoEnabled = this.cgoEnabled
            copy.outputPath = this.outputPath
            copy.goProxy = this.goProxy
            copy.stdinFilePath = this.stdinFilePath
            copy.runInExternalTerminal = this.runInExternalTerminal
            copy.timeoutSeconds = this.timeoutSeconds
            return copy
        }
    }

    init {
        workingDirectory = project.basePath ?: ""

        // Pre-fill from globally cached Go env (populated during SDK detection)
        val registry = GoGlobalSdkRegistry.getInstance()
        if (registry.state.cachedGoOS.isEmpty()) {
            val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
            if (sdkPath.isNotEmpty()) {
                registry.refreshEnvCache(sdkPath)
            }
        }
        if (goOS.isEmpty()) goOS = registry.state.cachedGoOS
        if (goArch.isEmpty()) goArch = registry.state.cachedGoArch
        if (goProxy.isEmpty()) goProxy = registry.state.cachedGoProxy
    }
}