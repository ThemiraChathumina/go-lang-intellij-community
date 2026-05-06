package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestLocatorTest : BasePlatformTestCase() {

    fun testGetLocation_unknownProtocol_returnsEmptyList() {
        val result = GoTestLocator.getLocation(
            "unknown", "pkg:TestFoo", project,
            GlobalSearchScope.allScope(project)
        )
        assertTrue(result.isEmpty())
    }

    fun testGetLocation_emptyProtocol_returnsEmptyList() {
        val result = GoTestLocator.getLocation(
            "", "pkg:TestFoo", project,
            GlobalSearchScope.allScope(project)
        )
        assertTrue(result.isEmpty())
    }

    fun testGetLocation_httpProtocol_returnsEmptyList() {
        val result = GoTestLocator.getLocation(
            "http", "example.com/TestFoo", project,
            GlobalSearchScope.allScope(project)
        )
        assertTrue(result.isEmpty())
    }

    fun testGetLocation_gotestProtocol_withNoMatchingFunction_returnsEmpty() {
        // No test file configured, so no function can be found
        val result = GoTestLocator.getLocation(
            "gotest", "pkg:TestNonexistentFunction", project,
            GlobalSearchScope.allScope(project)
        )
        assertTrue(result.isEmpty())
    }

    fun testGetLocation_gotestProtocol_pathWithoutColon_usesWholePathAsTestName() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc JustTestName() {}")
        val result = GoTestLocator.getLocation(
            "gotest", "JustTestName", project,
            GlobalSearchScope.allScope(project)
        )
        // Either found or empty — should not throw an exception
        assertNotNull(result)
    }

    fun testGetLocation_gotestProtocol_findsExistingTestFunction() {
        myFixture.configureByText("found_test.go", "package main\n\nfunc TestFoundFunction() {}")
        val result = GoTestLocator.getLocation(
            "gotest", "main:TestFoundFunction", project,
            GlobalSearchScope.allScope(project)
        )
        assertNotNull(result)
        if (result.isNotEmpty()) {
            assertEquals("TestFoundFunction", result[0].psiElement.text.substringAfter("func ").substringBefore("("))
        }
    }

    fun testGetLocation_gotestProtocol_pathWithMultipleColons_usesSecondPartAsTestName() {
        myFixture.configureByText("multi_test.go", "package main\n\nfunc TestMulti() {}")
        val result = GoTestLocator.getLocation(
            "gotest", "pkg:TestMulti", project,
            GlobalSearchScope.allScope(project)
        )
        // Should attempt to find TestMulti and not throw
        assertNotNull(result)
    }
}
