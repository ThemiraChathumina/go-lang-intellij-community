package com.github.themirachathumina.golangintellijcommunity.run.test.coverage

import com.github.themirachathumina.golangintellijcommunity.util.FakeFileSystemAccess
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Method

class GoModUtilsTest {

    @Test
    fun testReadModuleName() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/project/go.mod" to "module github.com/user/testmod\n\ngo 1.22\n")
        )
        val utils = GoModUtils(fs)
        
        val method: Method = GoModUtils::class.java.getDeclaredMethod("readModuleName", String::class.java)
        method.isAccessible = true
        val result = method.invoke(utils, "/project/go.mod") as String?
        
        assertEquals("github.com/user/testmod", result)
    }

    @Test
    fun testFindGoModForFile() {
        val fs = FakeFileSystemAccess(
            files = mutableMapOf("/project/go.mod" to "module x", "/project/src/main.go" to "package main"),
            directories = mutableSetOf("/project", "/project/src")
        )
        val utils = GoModUtils(fs)
        
        val method: Method = GoModUtils::class.java.getDeclaredMethod("findGoModForFile", String::class.java)
        method.isAccessible = true
        val result = method.invoke(utils, "/project/src/main.go") as String?
        
        assertEquals("/project/go.mod", result)
    }
}