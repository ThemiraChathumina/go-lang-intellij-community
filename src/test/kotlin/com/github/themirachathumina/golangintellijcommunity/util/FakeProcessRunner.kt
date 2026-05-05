package com.github.themirachathumina.golangintellijcommunity.util

class FakeProcessRunner(
    val responses: MutableMap<List<String>, ProcessOutput> = mutableMapOf(),
    var defaultResponse: ProcessOutput = ProcessOutput(0, "", "")
) : ProcessRunner {
    override fun run(command: List<String>, workDir: String?, env: Map<String, String>, timeoutMs: Int): ProcessOutput {
        return responses[command] ?: defaultResponse
    }
}
