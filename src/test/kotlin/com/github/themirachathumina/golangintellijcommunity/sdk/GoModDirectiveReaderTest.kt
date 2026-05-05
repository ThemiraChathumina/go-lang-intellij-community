package com.github.themirachathumina.golangintellijcommunity.sdk

import com.github.themirachathumina.golangintellijcommunity.util.FakeFileSystemAccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoModDirectiveReaderTest {

    @Test
    fun `reads go directive from go mod`() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/project/go.mod" to "module test\n\ngo 1.22.0\n")
        )
        val reader = GoModDirectiveReader(fs)
        assertEquals("1.22.0", reader.readGoDirective("/project/go.mod"))
    }

    @Test
    fun `reads toolchain directive from go mod`() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/project/go.mod" to "module test\n\ngo 1.21\ntoolchain go1.22.1\n")
        )
        val reader = GoModDirectiveReader(fs)
        assertEquals("1.22.1", reader.readGoDirective("/project/go.mod"))
    }

    @Test
    fun `reads go version from go-version file`() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/project/.go-version" to "1.22.2\n")
        )
        val reader = GoModDirectiveReader(fs)
        assertEquals("1.22.2", reader.readGoVersionFile("/project/.go-version"))
    }

    @Test
    fun `reads go version from go-version file with go prefix`() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/project/.go-version" to "go1.22.3\n")
        )
        val reader = GoModDirectiveReader(fs)
        assertEquals("1.22.3", reader.readGoVersionFile("/project/.go-version"))
    }

    @Test
    fun `returns null if file does not exist`() {
        val fs = FakeFileSystemAccess()
        val reader = GoModDirectiveReader(fs)
        assertNull(reader.readGoDirective("/project/go.mod"))
        assertNull(reader.readGoVersionFile("/project/.go-version"))
    }
}
