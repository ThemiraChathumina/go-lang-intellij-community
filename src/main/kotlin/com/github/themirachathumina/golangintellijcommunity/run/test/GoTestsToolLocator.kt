package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.sdk.GoEnvironmentReader
import com.intellij.openapi.util.SystemInfo
import java.io.File

/*
 * Locates the gotests binary by scanning GOPATH/bin directories and PATH.
 * Returns null when gotests is not installed, which triggers an install prompt.
 */
object GoTestsToolLocator {

    private val GOTESTS_NAME = if (SystemInfo.isWindows) "${GoConstants.Executables.GOTESTS}.exe"
                               else GoConstants.Executables.GOTESTS

    fun findGotests(): String? {
        val gopathEntries = buildList {
            val goEnv = GoEnvironmentReader.readGoEnvFile()
            goEnv[GoConstants.Env.GOPATH]?.split(File.pathSeparator)?.forEach { add(it) }
            System.getenv(GoConstants.Env.GOPATH)?.split(File.pathSeparator)?.forEach { add(it) }
            add(File(System.getProperty("user.home"), "go").path)
        }.distinct()

        for (gopathEntry in gopathEntries) {
            val candidate = File(gopathEntry, "bin${File.separator}$GOTESTS_NAME")
            if (candidate.exists() && candidate.canExecute()) return candidate.absolutePath
        }

        return try {
            val proc = ProcessBuilder(GOTESTS_NAME, "--help").redirectErrorStream(true).start()
            proc.waitFor()
            GOTESTS_NAME
        } catch (_: Exception) {
            null
        }
    }
}
