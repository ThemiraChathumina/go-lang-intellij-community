package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.util.FakeFileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.FakeProcessRunner
import com.github.themirachathumina.golangintellijcommunity.util.ProcessOutput
import com.intellij.openapi.util.SystemInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoSdkLocatorTest {
    private val goExeName = if (SystemInfo.isWindows) "go.exe" else "go"
    private val goplsExeName = if (SystemInfo.isWindows) "gopls.exe" else "gopls"

    @Test
    fun `detects go executable in bin folder`() {
        val goPath = "/usr/local/go/bin/$goExeName"
        val fs = FakeFileSystemAccess(
            files = mutableMapOf(goPath to ""),
            executables = mutableSetOf(goPath),
            directories = mutableSetOf("/usr/local/go", "/usr/local/go/bin")
        )
        val locator = GoSdkLocator(fs = fs)
        assertEquals(goPath, locator.getGoExecutablePath("/usr/local/go"))
    }

    @Test
    fun `detects gopls in sdk bin`() {
        val goplsPath = "/usr/local/go/bin/$goplsExeName"
        val fs = FakeFileSystemAccess(
            files = mutableMapOf(goplsPath to ""),
            executables = mutableSetOf(goplsPath),
            directories = mutableSetOf("/usr/local/go/bin")
        )
        val locator = GoSdkLocator(fs = fs)
        assertEquals(goplsPath, locator.getGoplsExecutablePath("/usr/local/go"))
    }

    @Test
    fun `suggests sdk paths from goroot env`() {
        val goPath = "/usr/local/go/bin/$goExeName"
        val fs = FakeFileSystemAccess(
            env = mutableMapOf("GOROOT" to "/usr/local/go"),
            files = mutableMapOf(goPath to ""),
            executables = mutableSetOf(goPath),
            directories = mutableSetOf("/usr/local/go", "/usr/local/go/bin")
        )
        val locator = GoSdkLocator(fs = fs)
        val suggestions = locator.suggestSdkHomePaths()
        assertTrue(suggestions.contains("/usr/local/go"))
    }

    @Test
    fun `suggests sdk paths from common unix locations`() {
        val goPath = "/usr/local/go/bin/$goExeName"
        val fs = FakeFileSystemAccess(
            files = mutableMapOf(goPath to ""),
            executables = mutableSetOf(goPath),
            directories = mutableSetOf("/usr/local/go", "/usr/local/go/bin")
        )
        val locator = GoSdkLocator(fs = fs, isWindows = false)
        val suggestions = locator.suggestSdkHomePaths()
        assertTrue(suggestions.contains("/usr/local/go"))
    }

    @Test
    fun `checks if gopls is installed via version command`() {
        val runner = FakeProcessRunner(
            responses = mutableMapOf(
                listOf(goplsExeName, "version") to ProcessOutput(0, "gopls version v0.15.3", "")
            )
        )
        val fs = FakeFileSystemAccess()
        val locator = GoSdkLocator(fs = fs, runner = runner)
        assertTrue(locator.isGoplsInstalled(null))
    }
}
