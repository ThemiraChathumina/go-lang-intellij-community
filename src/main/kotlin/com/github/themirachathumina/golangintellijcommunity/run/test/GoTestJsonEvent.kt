package com.github.themirachathumina.golangintellijcommunity.run.test

import com.google.gson.annotations.SerializedName

/**
 * Data class representing an event from 'go test -json' output.
 */
data class GoTestJsonEvent(
    @SerializedName("Time")
    val time: String? = null,

    @SerializedName("Action")
    val action: String,

    @SerializedName("Package")
    val packageName: String? = null,

    @SerializedName("Test")
    val testName: String? = null,

    @SerializedName("Elapsed")
    val elapsed: Double? = null,

    @SerializedName("Output")
    val output: String? = null
)
