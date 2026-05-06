package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil

/*
 * Launches a compiled Go binary in the system's external terminal (Windows cmd, macOS Terminal,
 * or Linux x-terminal-emulator), generating a wrapper .bat script on Windows.
 */
object GoExternalTerminalLauncher {

    fun create(exePath: String, config: GoRunConfiguration): GeneralCommandLine {
        return if (SystemInfo.isWindows) {
            val tempBat = FileUtil.createTempFile("go_run_", ".bat", true)
            val batContent = """
                @echo off
                "$exePath" ${config.programArguments}
                if %errorlevel% neq 0 (
                    echo.
                    echo Process finished with exit code %errorlevel%
                )
                pause
            """.trimIndent()
            FileUtil.writeToFile(tempBat, batContent)
            GeneralCommandLine("cmd.exe", "/c", "start", "/wait", tempBat.absolutePath)
        } else if (SystemInfo.isMac) {
            GeneralCommandLine("open", "-a", "Terminal", exePath)
        } else {
            GeneralCommandLine("x-terminal-emulator", "-e", "\"$exePath\" ${config.programArguments}")
        }
    }
}
