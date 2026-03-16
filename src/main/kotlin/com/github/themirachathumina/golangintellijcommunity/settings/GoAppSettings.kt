package com.github.themirachathumina.golangintellijcommunity.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "GoAppSettings",
    storages = [Storage("goAppSettings.xml")]
)
class GoAppSettings : PersistentStateComponent<GoAppSettings.State> {

    class State {
        var lspEnabled: Boolean = true
        var semanticTokensEnabled: Boolean = false
        var logSemanticTokens: Boolean = false
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): GoAppSettings =
            ApplicationManager.getApplication().getService(GoAppSettings::class.java)
    }
}
