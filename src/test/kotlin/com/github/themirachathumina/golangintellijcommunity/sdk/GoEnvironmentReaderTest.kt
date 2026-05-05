package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.util.FakeFileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.FakeProcessRunner
import com.github.themirachathumina.golangintellijcommunity.util.ProcessOutput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoEnvironmentReaderTest {

    @Test
    fun `reads go env file on unix`() {
        val fs = FakeFileSystemAccess(
            userHomePath = "/home/user",
            files = mutableMapOf("/home/user/.config/go/env" to "GOPATH=/home/user/go\nGOROOT=/usr/local/go\n")
        )
        val reader = GoEnvironmentReader(fs)
        val env = reader.readGoEnvFile()
        assertEquals("/home/user/go", env["GOPATH"])
        assertEquals("/usr/local/go", env["GOROOT"])
    }

    @Test
    fun `respects GOENV environment variable`() {
        val fs = FakeFileSystemAccess(
            env = mutableMapOf("GOENV" to "/custom/go/env"),
            files = mutableMapOf("/custom/go/env" to "GOPATH=/custom/gopath\n")
        )
        val reader = GoEnvironmentReader(fs)
        val env = reader.readGoEnvFile()
        assertEquals("/custom/gopath", env["GOPATH"])
    }

    @Test
    fun `resolves go path with multiple sources`() {
        val fs = FakeFileSystemAccess(
            userHomePath = "/home/user",
            env = mutableMapOf("GOPATH" to "/env/gopath"),
            files = mutableMapOf("/home/user/.config/go/env" to "GOPATH=/file/gopath\n")
        )
        val reader = GoEnvironmentReader(fs)
        val paths = reader.resolveGoPath()
        assertTrue(paths.contains("/file/gopath"))
        assertTrue(paths.contains("/env/gopath"))
        assertTrue(paths.contains("/home/user/go"))
    }

    @Test
    fun `resolves go mod cache`() {
        val fs = FakeFileSystemAccess(
            env = mutableMapOf("GOMODCACHE" to "/custom/modcache")
        )
        val reader = GoEnvironmentReader(fs)
        assertEquals("/custom/modcache", reader.resolveGoModCache())
    }

    @Test
    fun `fetches go env using process runner`() {
        val runner = FakeProcessRunner(
            responses = mutableMapOf(
                listOf("go", "env", "GOROOT", "GOPATH") to ProcessOutput(0, "/usr/local/go\n/home/user/go\n", "")
            )
        )
        val reader = GoEnvironmentReader(runner = runner)
        val env = reader.fetchGoEnv("go", "GOROOT", "GOPATH")
        assertEquals("/usr/local/go", env["GOROOT"])
        assertEquals("/home/user/go", env["GOPATH"])
    }
}
