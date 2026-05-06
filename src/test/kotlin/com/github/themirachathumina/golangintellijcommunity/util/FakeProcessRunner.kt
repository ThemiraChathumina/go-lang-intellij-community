package com.github.themirachathumina.golangintellijcommunity.util

/*
 * Mock implementation of ProcessRunner for unit testing. 
 * Allows tests to define expected command outputs and exit codes, simulating 
 * the execution of external binaries like 'go' or 'gopls'.
 */
class FakeProcessRunner(
    val responses: MutableMap<List<String>, ProcessOutput> = mutableMapOf(),
    var defaultResponse: ProcessOutput = ProcessOutput(0, "", "")
) : ProcessRunner {
    override fun run(command: List<String>, workDir: String?, env: Map<String, String>, timeoutMs: Int): ProcessOutput {
        return responses[command] ?: defaultResponse
    }
}
