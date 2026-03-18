package com.github.themirachathumina.golangintellijcommunity.run.test

import com.google.gson.Gson

/** Escapes special characters for TeamCity service message values. */
private fun tcEscape(text: String): String = text
    .replace("|", "||")
    .replace("'", "|'")
    .replace("\n", "|n")
    .replace("\r", "|r")
    .replace("[", "|[")
    .replace("]", "|]")

/**
 * Retranslates Go test JSON output into TeamCity service messages.
 *
 * Subtests (e.g. `TestDivide/positive`) are shown as children of their parent
 * in the SMTestRunner tree. The implementation buffers every `run` event;
 * when the first child of a test appears, the parent is promoted to a suite
 * (`testSuiteStarted`). If no child ever appears, the test is emitted as a
 * leaf (`testStarted`) when its `pass`/`fail`/`skip` event arrives.
 *
 * `nodeId` / `parentNodeId` attributes let IntelliJ build the tree without
 * any name-parsing heuristics.
 */
/**
 * @param onTestResult Called when a top-level or subtest result is finalised.
 *   [passed] is `true` (passed), `false` (failed), or `null` (skipped).
 *   The [testName] is the full Go test name (e.g. "TestDivide", "FuzzReverse/seed#0").
 */
class GoTestJsonRetranslator(
    private val onTestResult: ((testName: String, passed: Boolean?) -> Unit)? = null
) {
    private val gson = Gson()

    // ---- Node ID management ------------------------------------------------

    private var nextNodeId = 1
    private val nodeIds = mutableMapOf<String, Int>()

    /** nodeId for each package suite; keyed by package name. */
    private val packageNodeIds = mutableMapOf<String, Int>()

    private fun getNodeId(key: String): Int = nodeIds.getOrPut(key) { nextNodeId++ }

    // ---- Pending / suite state ---------------------------------------------

    private data class PendingEntry(
        val displayName: String,      // short segment: "positive" for "TestDivide/positive"
        val parentTestName: String?,  // full name of parent test; null = package is parent
        val packageName: String,
        val outputs: MutableList<String> = mutableListOf()
    )

    /** Tests whose `run` event has been seen but not yet emitted. */
    private val pending = mutableMapOf<String, PendingEntry>()

    /** Full test names that have been emitted as `testSuiteStarted`. */
    private val suites = mutableSetOf<String>()

    /** Display names for promoted suites (needed for `testSuiteFinished`). */
    private val displayNames = mutableMapOf<String, String>()

    // ---- Per-event output accumulator --------------------------------------

    private val eventOut = StringBuilder()
    private fun emit(msg: String) { eventOut.append(msg) }

    // ---- Public API --------------------------------------------------------

    /**
     * Translates a single output line.
     *  - Returns TeamCity service messages for recognised JSON events.
     *  - Returns the original text for non-JSON lines.
     *  - Returns null for events that should be suppressed.
     */
    fun translateLine(text: String): String? {
        val trimmed = text.trim()
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) return text
        return try {
            eventOut.clear()
            val event = gson.fromJson(trimmed, GoTestJsonEvent::class.java)
            processJsonEvent(event)
            if (eventOut.isEmpty()) null else eventOut.toString()
        } catch (e: Exception) {
            text
        }
    }

    // ---- Event routing -----------------------------------------------------

    private fun processJsonEvent(event: GoTestJsonEvent) {
        val testName = event.testName
        val packageName = event.packageName ?: ""
        if (testName == null) {
            processPackageEvent(event, packageName)
            return
        }
        when (event.action) {
            "run"    -> onTestRun(testName, packageName)
            "output" -> onTestOutput(testName, event.output ?: "")
            "pass"   -> onTestResult(testName, "pass", event.elapsed ?: 0.0)
            "fail"   -> onTestResult(testName, "fail", event.elapsed ?: 0.0)
            "skip"   -> onTestResult(testName, "skip", event.elapsed ?: 0.0)
        }
    }

    // ---- Package-level events ----------------------------------------------

    private fun processPackageEvent(event: GoTestJsonEvent, packageName: String) {
        val name = tcEscape(packageName)
        when (event.action) {
            "run" -> {
                val id = getNodeId("pkg:$packageName")
                packageNodeIds[packageName] = id
                emit("##teamcity[testSuiteStarted name='$name' nodeId='$id']\n")
            }
            "pass", "fail", "skip" -> {
                val id = packageNodeIds[packageName] ?: return
                emit("##teamcity[testSuiteFinished name='$name' nodeId='$id']\n")
            }
            "output" -> {
                val output = event.output ?: ""
                if (output.isNotEmpty()) emit(output)
            }
        }
    }

    // ---- Test-level events -------------------------------------------------

    private fun onTestRun(fullName: String, packageName: String) {
        val slashIdx = fullName.lastIndexOf('/')
        val displayName = if (slashIdx >= 0) fullName.substring(slashIdx + 1) else fullName
        val parentTestName = if (slashIdx >= 0) fullName.substring(0, slashIdx) else null
        pending[fullName] = PendingEntry(displayName, parentTestName, packageName)
        // Buffered — not emitted until we know whether this test has subtests.
    }

    private fun onTestOutput(fullName: String, output: String) {
        if (output.isBlank()) return
        when {
            fullName in pending -> pending[fullName]!!.outputs.add(output)
            fullName in suites  -> {
                val id   = getNodeId(fullName)
                val name = tcEscape(displayNames[fullName] ?: fullName)
                emit("##teamcity[testStdOut name='$name' nodeId='$id' out='${tcEscape(output)}']\n")
            }
            else -> emit("##teamcity[testStdOut name='${tcEscape(fullName)}' out='${tcEscape(output)}']\n")
        }
    }

    private fun onTestResult(fullName: String, result: String, elapsed: Double) {
        val duration = (elapsed * 1000).toLong()
        val passed = when (result) { "pass" -> true; "fail" -> false; else -> null }
        when {
            fullName in suites -> {
                val id   = getNodeId(fullName)
                val name = tcEscape(displayNames[fullName] ?: fullName)
                emit("##teamcity[testSuiteFinished name='$name' nodeId='$id']\n")
                onTestResult?.invoke(fullName, passed)
            }
            fullName in pending -> {
                val entry        = pending.remove(fullName)!!
                val parentNodeId = resolveParentNodeId(entry.parentTestName, entry.packageName)
                val id           = getNodeId(fullName)
                val name         = tcEscape(entry.displayName)
                val hint         = "gotest://${tcEscape(entry.packageName)}:${tcEscape(fullName)}"

                emit("##teamcity[testStarted name='$name' nodeId='$id' parentNodeId='$parentNodeId' locationHint='$hint']\n")

                for (out in entry.outputs) {
                    emit("##teamcity[testStdOut name='$name' nodeId='$id' out='${tcEscape(out)}']\n")
                    parseBenchmarkStats(out.trim())?.let { emitBenchStats(it, entry.displayName, id) }
                }
                when (result) {
                    "fail" -> emit("##teamcity[testFailed name='$name' nodeId='$id' message='Test failed']\n")
                    "skip" -> emit("##teamcity[testIgnored name='$name' nodeId='$id' message='Test skipped']\n")
                }
                if (result != "skip") {
                    emit("##teamcity[testFinished name='$name' nodeId='$id' duration='$duration']\n")
                }
                onTestResult?.invoke(fullName, passed)
            }
            // else: unmatched result — ignore
        }
    }

    // ---- Suite promotion ---------------------------------------------------

    /**
     * Returns the nodeId that should be used as `parentNodeId` for a test.
     * If [parentTestName] is still pending it is promoted to a suite first.
     */
    private fun resolveParentNodeId(parentTestName: String?, packageName: String): Int {
        if (parentTestName == null) return packageNodeIds[packageName] ?: 0
        return ensureSuiteStarted(parentTestName, packageName)
    }

    /**
     * Promotes [fullName] from a pending leaf to a suite and emits
     * `testSuiteStarted` (plus any buffered output). Returns the suite's nodeId.
     * If [fullName] is already a suite, returns its nodeId immediately.
     */
    private fun ensureSuiteStarted(fullName: String, packageName: String): Int {
        if (fullName in suites) return getNodeId(fullName)

        val entry = pending.remove(fullName)
            ?: return packageNodeIds[packageName] ?: 0   // defensive fallback

        val grandParentNodeId = resolveParentNodeId(entry.parentTestName, entry.packageName)
        val id = getNodeId(fullName)

        suites.add(fullName)
        displayNames[fullName] = entry.displayName

        emit("##teamcity[testSuiteStarted name='${tcEscape(entry.displayName)}' nodeId='$id' parentNodeId='$grandParentNodeId']\n")
        for (out in entry.outputs) {
            emit("##teamcity[testStdOut name='${tcEscape(entry.displayName)}' nodeId='$id' out='${tcEscape(out)}']\n")
        }
        return id
    }

    // ---- Benchmark stats ---------------------------------------------------

    /**
     * Benchmark result line pattern emitted by `go test -bench`.
     * Example: "BenchmarkFoo-8   1000000   1234 ns/op   64 B/op   2 allocs/op"
     */
    private val benchmarkLineRegex = Regex(
        """^(Benchmark\S+)\s+(\d+)\s+([\d.]+)\s+ns/op(?:\s+([\d.]+)\s+B/op)?(?:\s+([\d.]+)\s+allocs/op)?"""
    )

    private data class BenchmarkStats(
        val name: String,
        val iterations: Long,
        val nsPerOp: Double,
        val bytesPerOp: Double?,
        val allocsPerOp: Double?
    )

    private fun parseBenchmarkStats(line: String): BenchmarkStats? {
        val match = benchmarkLineRegex.find(line) ?: return null
        val (name, iters, nsOp, bOp, allocOp) = match.destructured
        return BenchmarkStats(
            name         = name,
            iterations   = iters.toLongOrNull() ?: return null,
            nsPerOp      = nsOp.toDoubleOrNull() ?: return null,
            bytesPerOp   = bOp.takeIf { it.isNotEmpty() }?.toDoubleOrNull(),
            allocsPerOp  = allocOp.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        )
    }

    private fun emitBenchStats(stats: BenchmarkStats, displayName: String, nodeId: Int) {
        val escapedKey = tcEscape(stats.name)
        emit("##teamcity[buildStatisticValue key='$escapedKey ns/op' value='${stats.nsPerOp}']\n")
        stats.bytesPerOp?.let  { emit("##teamcity[buildStatisticValue key='$escapedKey B/op' value='$it']\n") }
        stats.allocsPerOp?.let { emit("##teamcity[buildStatisticValue key='$escapedKey allocs/op' value='$it']\n") }
        val summary = buildString {
            append("  Benchmark result: ${stats.iterations} iterations, ${stats.nsPerOp} ns/op")
            stats.bytesPerOp?.let  { append(", $it B/op") }
            stats.allocsPerOp?.let { append(", $it allocs/op") }
        }
        emit("##teamcity[testStdOut name='${tcEscape(displayName)}' nodeId='$nodeId' out='${tcEscape(summary)}']\n")
    }
}
