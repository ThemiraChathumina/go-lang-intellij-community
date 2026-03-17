package com.github.themirachathumina.golangintellijcommunity.run.test

import com.google.gson.Gson

/**
 * Retranslates Go test JSON output into TeamCity service messages.
 */
class GoTestJsonRetranslator {
    private val gson = Gson()

    /**
     * Translates a line of output. 
     * Returns the TeamCity message if it's a Go test JSON event, 
     * or the original text if it's not JSON (or we want to pass it through).
     * Returns null if the line is a JSON event that should be suppressed (e.g. metadata).
     */
    fun translateLine(text: String): String? {
        val trimmed = text.trim()
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            return text
        }

        return try {
            val event = gson.fromJson(trimmed, GoTestJsonEvent::class.java)
            processJsonEvent(event)
        } catch (e: Exception) {
            text
        }
    }

    private fun processJsonEvent(event: GoTestJsonEvent): String? {
        val testName = event.testName
        val packageName = event.packageName ?: ""

        if (testName == null) {
            return processPackageEvent(event, packageName)
        }

        val tcName = escape(testName)
        val locationHint = "gotest://$packageName:$testName"

        return when (event.action) {
            "run" -> "##teamcity[testStarted name='$tcName' locationHint='$locationHint']\n"
            "pass" -> {
                val duration = (event.elapsed ?: 0.0) * 1000
                "##teamcity[testFinished name='$tcName' duration='${duration.toLong()}']\n"
            }
            "fail" -> {
                val duration = (event.elapsed ?: 0.0) * 1000
                "##teamcity[testFailed name='$tcName' message='Test failed' duration='${duration.toLong()}']\n" +
                "##teamcity[testFinished name='$tcName' duration='${duration.toLong()}']\n"
            }
            "skip" -> "##teamcity[testIgnored name='$tcName' message='Test skipped']\n"
            "output" -> {
                val output = event.output ?: ""
                if (output.isNotEmpty()) {
                    "##teamcity[testStdOut name='$tcName' out='${escape(output)}']\n"
                } else null
            }
            else -> null
        }
    }

    private fun processPackageEvent(event: GoTestJsonEvent, packageName: String): String? {
        val tcPackage = escape(packageName)
        return when (event.action) {
            "run" -> "##teamcity[testSuiteStarted name='$tcPackage']\n"
            "pass", "fail", "skip" -> "##teamcity[testSuiteFinished name='$tcPackage']\n"
            "output" -> {
                val output = event.output ?: ""
                if (output.isNotEmpty()) {
                    // Forward package-level output (like TestMain output) as plain text
                    output
                } else null
            }
            else -> null
        }
    }

    private fun escape(text: String): String {
        return text.replace("|", "||")
            .replace("'", "|'")
            .replace("\n", "|n")
            .replace("\r", "|r")
            .replace("[", "|[")
            .replace("]", "|]")
    }
}
