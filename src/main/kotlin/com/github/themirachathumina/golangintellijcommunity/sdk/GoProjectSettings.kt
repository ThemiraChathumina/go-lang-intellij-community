package com.github.themirachathumina.golangintellijcommunity.sdk

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

interface GoProjectSettingsListener {
    fun sdkChanged(newPath: String)
}

@Service(Service.Level.PROJECT)
@State(
    name = "GoProjectSettings",
    storages = [Storage("goProjectSettings.xml")]
)
class GoProjectSettings(val project: Project) : PersistentStateComponent<GoProjectSettings.State> {
    class State {
        var goSdkPath: String = ""
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun setSdkPath(path: String) {
        if (myState.goSdkPath != path) {
            myState.goSdkPath = path
            project.messageBus.syncPublisher(TOPIC).sdkChanged(path)
        }
    }

    companion object {
        @Topic.ProjectLevel
        val TOPIC = Topic.create("Go SDK Changes", GoProjectSettingsListener::class.java)
        
        fun getInstance(project: Project): GoProjectSettings = project.getService(GoProjectSettings::class.java)
    }
}
