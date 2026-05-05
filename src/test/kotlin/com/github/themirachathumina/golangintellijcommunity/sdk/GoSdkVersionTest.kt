package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.util.FakeFileSystemAccess
import com.github.themirachathumina.golangintellijcommunity.util.FakeProcessRunner
import com.github.themirachathumina.golangintellijcommunity.util.ProcessOutput
import org.junit.Assert.*
import org.junit.Test

class GoSdkVersionTest {

    @Test
    fun testCompareVersions() {
        val sdkVersion = GoSdkVersion()
        assertTrue(sdkVersion.compareVersions("1.21", "1.20") > 0)
        assertTrue(sdkVersion.compareVersions("1.21.1", "1.21.0") > 0)
        assertTrue(sdkVersion.compareVersions("1.21", "1.21.0") == 0)
        assertTrue(sdkVersion.compareVersions("1.20", "1.21") < 0)
        assertTrue(sdkVersion.compareVersions("1.22", "1.21") > 0)
    }

    @Test
    fun testIsSupportedVersion() {
        val sdkVersion = GoSdkVersion()
        // GoConstants.Sdk.MIN_VERSION is "1.21"
        assertTrue(sdkVersion.isSupportedVersion("1.21"))
        assertTrue(sdkVersion.isSupportedVersion("1.21.5"))
        assertTrue(sdkVersion.isSupportedVersion("1.22"))
        assertFalse(sdkVersion.isSupportedVersion("1.20"))
        assertFalse(sdkVersion.isSupportedVersion("1.19.9"))
    }

    @Test
    fun testReadVersionFile() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/sdk/VERSION" to "go1.22.1"),
            directories = mutableSetOf("/sdk")
        )
        val sdkVersion = GoSdkVersion(fs = fs)
        
        // We need to use reflection or make readVersionFile internal/protected to test it directly
        // Or we can test it via retrieveGoVersion if we avoid the ApplicationManager call
    }
}
