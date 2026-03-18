package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

/**
 * Project-level service that stores the most recent pass/fail result for each
 * Go test function. Results are keyed by the test name exactly as it appears
 * in `go test -json` output (e.g. "TestDivide", "FuzzReverse").
 *
 * Implements PersistentStateComponent to save the results in workspace.xml
 * so gutter icons survive IDE restarts.
 */
@State(
    name = "GoTestResults",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)] // Saves to workspace.xml (not checked into VCS)
)
@Service(Service.Level.PROJECT)
class GoTestResultsService : PersistentStateComponent<GoTestResultsService.State> {

    enum class Status { PASSED, FAILED, SKIPPED }

    /** * The state class must have a default constructor and mutable properties
     * for the XML serializer to work.
     */
    class State {
        var testStatuses: MutableMap<String, Status> = mutableMapOf()
    }

    private var myState = State()

    // Kept for thread safety while tests are actively running and writing results
    private val results = ConcurrentHashMap<String, Status>()

    override fun getState(): State {
        // Copy the concurrent map into the serializable state object before saving
        myState.testStatuses.clear()
        myState.testStatuses.putAll(results)
        return myState
    }

    override fun loadState(state: State) {
        // Load the saved state from XML into memory
        myState = state
        results.clear()
        results.putAll(state.testStatuses)
    }

    fun record(testName: String, status: Status) {
        results[testName] = status
    }

    fun getStatus(testName: String): Status? = results[testName]

    companion object {
        fun getInstance(project: Project): GoTestResultsService =
            project.getService(GoTestResultsService::class.java)
    }
}