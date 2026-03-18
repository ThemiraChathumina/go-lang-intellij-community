package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.concurrent.ConcurrentHashMap

/**
 * Project-level service that stores the most recent pass/fail result for each
 * Go test function.  Results are keyed by the test name exactly as it appears
 * in `go test -json` output (e.g. "TestDivide", "FuzzReverse").
 *
 * [GoTestRunLineMarkerContributor] reads from here to choose the gutter icon.
 * Results are populated by the SMTRunnerEventsListener wired up in
 * [GoTestCommandLineState.execute].
 */
@Service(Service.Level.PROJECT)
class GoTestResultsService {

    enum class Status { PASSED, FAILED, SKIPPED }

    private val results = ConcurrentHashMap<String, Status>()

    fun record(testName: String, status: Status) {
        results[testName] = status
    }

    fun getStatus(testName: String): Status? = results[testName]

    companion object {
        fun getInstance(project: Project): GoTestResultsService =
            project.getService(GoTestResultsService::class.java)
    }
}
