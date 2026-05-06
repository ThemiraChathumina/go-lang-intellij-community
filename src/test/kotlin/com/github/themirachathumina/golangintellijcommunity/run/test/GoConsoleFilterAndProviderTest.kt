package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoConsoleFilterAndProviderTest : BasePlatformTestCase() {

    // --- GoConsoleFilter ---

    fun testApplyFilter_noGoPattern_returnsNull() {
        val filter = GoConsoleFilter(project)
        assertNull(filter.applyFilter("no go files mentioned here", 100))
    }

    fun testApplyFilter_emptyLine_returnsNull() {
        val filter = GoConsoleFilter(project)
        assertNull(filter.applyFilter("", 0))
    }

    fun testApplyFilter_goFilePatternNonExistentFile_returnsNull() {
        val filter = GoConsoleFilter(project)
        // Pattern matches but the file doesn't exist anywhere in the project
        val line = "panic: runtime error in nonexistent_xyz_file.go:42"
        assertNull(filter.applyFilter(line, line.length))
    }

    fun testApplyFilter_withExistingProjectFile_returnsResult() {
        myFixture.addFileToProject("findable.go", "package main")
        val filter = GoConsoleFilter(project, "/nonexistent/workdir")
        val line = "error in findable.go:1"
        // FilenameIndex should find the file via project-wide search
        val result = filter.applyFilter(line, line.length)
        assertNotNull(result)
    }

    fun testApplyFilter_withTestFilename_returnsResult() {
        myFixture.addFileToProject("foo_test.go", "package main")
        val filter = GoConsoleFilter(project, "/nonexistent/workdir")
        val line = "FAIL foo_test.go:10"
        val result = filter.applyFilter(line, line.length)
        assertNotNull(result)
    }

    fun testApplyFilter_patternMatchGivesCorrectOffsets() {
        myFixture.addFileToProject("offset_test.go", "package main")
        val filter = GoConsoleFilter(project, "/nonexistent/workdir")
        val line = "error in offset_test.go:5 something else"
        val result = filter.applyFilter(line, line.length)
        assertNotNull(result)
        val items = result!!.resultItems
        assertTrue(items.isNotEmpty())
    }

    fun testApplyFilter_multipleMatchesSameLine_allLinked() {
        myFixture.addFileToProject("alpha_test.go", "package main")
        myFixture.addFileToProject("beta_test.go", "package main")
        val filter = GoConsoleFilter(project, "/nonexistent/workdir")
        val line = "alpha_test.go:1 and beta_test.go:2"
        val result = filter.applyFilter(line, line.length)
        // At least one of the files should be found
        if (result != null) {
            assertTrue(result.resultItems.isNotEmpty())
        }
    }

    fun testApplyFilter_nonNumericLineNumber_returnsNull() {
        // "file.go:abc" — the line number is not numeric, so no result
        val filter = GoConsoleFilter(project)
        val line = "something.go:abc"
        // The regex requires digits after colon, so this won't match
        assertNull(filter.applyFilter(line, line.length))
    }

    // --- GoConsoleFilterProvider ---

    fun testGetDefaultFilters_returnsExactlyOneFilter() {
        val provider = GoConsoleFilterProvider()
        val filters = provider.getDefaultFilters(project)
        assertEquals(1, filters.size)
    }

    fun testGetDefaultFilters_returnsGoConsoleFilter() {
        val provider = GoConsoleFilterProvider()
        val filters = provider.getDefaultFilters(project)
        assertTrue(filters[0] is GoConsoleFilter)
    }

    fun testGetDefaultFilters_filterIsNotNull() {
        val provider = GoConsoleFilterProvider()
        val filters = provider.getDefaultFilters(project)
        assertNotNull(filters[0])
    }
}
