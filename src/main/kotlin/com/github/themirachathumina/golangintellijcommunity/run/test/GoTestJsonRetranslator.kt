package com.github.themirachathumina.golangintellijcommunity.run.test

import com.google.gson.Gson
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
 */
class GoTestJsonRetranslator(
    private val onTestResult: ((testName: String, passed: Boolean?) -> Unit)? = null
) {
    private val gson = Gson()

    // TeamCity expects timestamps in this exact format. UTC is used to safely format the 'Z' offset.
    private val tcTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.of("UTC"))

    // ---- Node ID management ------------------------------------------------

    private var nextNodeId = 1
    private val nodeIds = mutableMapOf<String, Int>()
    private val packageNodeIds = mutableMapOf<String, Int>()

    private fun getNodeId(key: String): Int = nodeIds.getOrPut(key) { nextNodeId++ }

    // ---- Pending / suite state ---------------------------------------------

    private data class PendingEntry(
        val displayName: String,
        val parentTestName: String?,
        val packageName: String,
        val startTime: Instant?, // Store as Instant to easily calculate duration later
        val outputs: MutableList<String> = mutableListOf()
    )

    private val pending = mutableMapOf<String, PendingEntry>()
    private val suites = mutableSetOf<String>()
    private val displayNames = mutableMapOf<String, String>()

    // ---- Per-event output accumulator --------------------------------------

    private val eventOut = StringBuilder()
    private fun emit(msg: String) { eventOut.append(msg) }

    private fun parseTime(timeStr: String?): Instant? {
        if (timeStr.isNullOrBlank()) return null
        return try { Instant.parse(timeStr) } catch (e: Exception) { null }
    }

    private fun formatTcTimestamp(instant: Instant?): String {
        if (instant == null) return ""
        return try {
            " timestamp='${tcTimeFormatter.format(instant)}'"
        } catch (e: Exception) {
            ""
        }
    }

    // ---- Public API --------------------------------------------------------

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
            "run"    -> onTestRun(testName, packageName, event.time)
            "output" -> onTestOutput(testName, event.output ?: "", event.time)
            "pass"   -> onTestResult(testName, "pass", event.elapsed ?: 0.0, event.time)
            "fail"   -> onTestResult(testName, "fail", event.elapsed ?: 0.0, event.time)
            "skip"   -> onTestResult(testName, "skip", event.elapsed ?: 0.0, event.time)
        }
    }

    // ---- Package-level events ----------------------------------------------

    private fun processPackageEvent(event: GoTestJsonEvent, packageName: String) {
        val name = tcEscape(packageName)
        val instant = parseTime(event.time)
        val ts = formatTcTimestamp(instant)
        when (event.action) {
            "run" -> {
                val id = getNodeId("pkg:$packageName")
                packageNodeIds[packageName] = id
                emit("##teamcity[testSuiteStarted name='$name' nodeId='$id'$ts]\n")
            }
            "pass", "fail", "skip" -> {
                val id = packageNodeIds[packageName] ?: return
                emit("##teamcity[testSuiteFinished name='$name' nodeId='$id'$ts]\n")
            }
            "output" -> {
                val output = event.output ?: ""
                if (output.isNotEmpty()) emit(output)
            }
        }
    }

    // ---- Test-level events -------------------------------------------------

    private fun onTestRun(fullName: String, packageName: String, time: String?) {
        val slashIdx = fullName.lastIndexOf('/')
        val displayName = if (slashIdx >= 0) fullName.substring(slashIdx + 1) else fullName
        val parentTestName = if (slashIdx >= 0) fullName.substring(0, slashIdx) else null
        pending[fullName] = PendingEntry(displayName, parentTestName, packageName, parseTime(time))
    }

    private fun onTestOutput(fullName: String, output: String, time: String?) {
        if (output.isBlank()) return
        val ts = formatTcTimestamp(parseTime(time))
        when {
            fullName in pending -> pending[fullName]!!.outputs.add(output)
            fullName in suites  -> {
                val id   = getNodeId(fullName)
                val name = tcEscape(displayNames[fullName] ?: fullName)
                emit("##teamcity[testStdOut name='$name' nodeId='$id' out='${tcEscape(output)}'$ts]\n")
            }
            else -> emit("##teamcity[testStdOut name='${tcEscape(fullName)}' out='${tcEscape(output)}'$ts]\n")
        }
    }

    private fun onTestResult(fullName: String, result: String, elapsed: Double, time: String?) {
        val passed = when (result) { "pass" -> true; "fail" -> false; else -> null }
        val endInstant = parseTime(time)
        val finishTs = formatTcTimestamp(endInstant)

        when {
            fullName in suites -> {
                val id   = getNodeId(fullName)
                val name = tcEscape(displayNames[fullName] ?: fullName)
                emit("##teamcity[testSuiteFinished name='$name' nodeId='$id'$finishTs]\n")
                onTestResult?.invoke(fullName, passed)
            }
            fullName in pending -> {
                val entry        = pending.remove(fullName)!!
                val parentNodeId = resolveParentNodeId(entry.parentTestName, entry.packageName, entry.startTime)
                val id           = getNodeId(fullName)
                val name         = tcEscape(entry.displayName)
                val hint         = "gotest://${tcEscape(entry.packageName)}:${tcEscape(fullName)}"

                val startTs = formatTcTimestamp(entry.startTime)

                emit("##teamcity[testStarted name='$name' nodeId='$id' parentNodeId='$parentNodeId' locationHint='$hint'$startTs]\n")

                for (out in entry.outputs) {
                    emit("##teamcity[testStdOut name='$name' nodeId='$id' out='${tcEscape(out)}']\n")
                    parseBenchmarkStats(out.trim())?.let { emitBenchStats(it, entry.displayName, id) }
                }

                // calculate explicit duration manually since Go omits Elapsed for individual tests
                var durationMs = (elapsed * 1000).toLong()
                if (durationMs <= 0L && entry.startTime != null && endInstant != null) {
                    durationMs = Duration.between(entry.startTime, endInstant).toMillis()
                }
                if (durationMs < 0) durationMs = 0

                when (result) {
                    "fail" -> emit("##teamcity[testFailed name='$name' nodeId='$id' message='Test failed'$finishTs]\n")
                    "skip" -> emit("##teamcity[testIgnored name='$name' nodeId='$id' message='Test skipped'$finishTs]\n")
                }
                if (result != "skip") {
                    emit("##teamcity[testFinished name='$name' nodeId='$id' duration='$durationMs'$finishTs]\n")
                }
                onTestResult?.invoke(fullName, passed)
            }
        }
    }

    // ---- Suite promotion ---------------------------------------------------

    private fun resolveParentNodeId(parentTestName: String?, packageName: String, childStartTime: Instant?): Int {
        if (parentTestName == null) return packageNodeIds[packageName] ?: 0
        return ensureSuiteStarted(parentTestName, packageName, childStartTime)
    }

    private fun ensureSuiteStarted(fullName: String, packageName: String, fallbackTime: Instant?): Int {
        if (fullName in suites) return getNodeId(fullName)

        val entry = pending.remove(fullName)
            ?: return packageNodeIds[packageName] ?: 0

        val grandParentNodeId = resolveParentNodeId(entry.parentTestName, entry.packageName, entry.startTime)
        val id = getNodeId(fullName)

        suites.add(fullName)
        displayNames[fullName] = entry.displayName

        val startTs = formatTcTimestamp(entry.startTime ?: fallbackTime)
        emit("##teamcity[testSuiteStarted name='${tcEscape(entry.displayName)}' nodeId='$id' parentNodeId='$grandParentNodeId'$startTs]\n")
        for (out in entry.outputs) {
            emit("##teamcity[testStdOut name='${tcEscape(entry.displayName)}' nodeId='$id' out='${tcEscape(out)}']\n")
        }
        return id
    }

    // ---- Benchmark stats ---------------------------------------------------

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