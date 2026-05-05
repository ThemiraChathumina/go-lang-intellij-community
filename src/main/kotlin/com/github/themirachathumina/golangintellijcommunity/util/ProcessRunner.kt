package com.github.themirachathumina.golangintellijcommunity.util

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.util.io.FileUtil

interface ProcessRunner {
    fun run(command: List<String>, workDir: String? = null, env: Map<String, String> = emptyMap(), timeoutMs: Int = 5000): ProcessOutput
}

data class ProcessOutput(val exitCode: Int, val stdout: String, val stderr: String)

class RealProcessRunner : ProcessRunner {
    override fun run(command: List<String>, workDir: String?, env: Map<String, String>, timeoutMs: Int): ProcessOutput {
        return try {
            val cmd = GeneralCommandLine(command)
            if (workDir != null) {
                cmd.withWorkDirectory(FileUtil.toSystemDependentName(workDir))
            }
            cmd.withEnvironment(env)
            val handler = CapturingProcessHandler(cmd)
            val output = handler.runProcess(timeoutMs)
            ProcessOutput(output.exitCode, output.stdout, output.stderr)
        } catch (e: Exception) {
            ProcessOutput(-1, "", e.message ?: "Unknown error")
        }
    }
}
