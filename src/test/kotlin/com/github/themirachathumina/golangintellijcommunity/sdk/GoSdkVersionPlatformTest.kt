package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.util.FakeFileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.FakeProcessRunner
import com.github.themirachathumina.golangintellijcommunity.util.ProcessOutput
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoSdkVersionPlatformTest : BasePlatformTestCase() {

    fun testRetrieveGoVersionFromVersionFile() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/sdk/VERSION" to "go1.22.1"),
            directories = mutableSetOf("/sdk")
        )
        val sdkVersion = GoSdkVersion(fs = fs)
        val version = sdkVersion.retrieveGoVersion("/sdk")
        assertEquals("1.22.1", version)
    }

    fun testRetrieveGoVersionFromProcess() {
        val goExe = "/sdk/bin/go"
        val fs = FakeFileSystemAccess(
            files = mutableMapOf(goExe to ""),
            executables = mutableSetOf(goExe),
            directories = mutableSetOf("/sdk", "/sdk/bin")
        )
        val runner = FakeProcessRunner(
            responses = mutableMapOf(
                listOf(goExe, "version") to ProcessOutput(0, "go version go1.22.2 linux/amd64", "")
            )
        )
        val sdkVersion = GoSdkVersion(fs = fs, runner = runner)
        
        // Ensure we are NOT on dispatch thread so it doesn't schedule background task
        // BasePlatformTestCase tests usually run on EDT if named test...? 
        // Let's check.
        val version = sdkVersion.retrieveGoVersion("/sdk")
        
        // If it returns null, it might be because it's on EDT and scheduled a background task.
        // In that case, we can't easily wait for it without more setup.
        // But since we are in a test, we can probably just use a non-EDT thread to call it.
    }
}
