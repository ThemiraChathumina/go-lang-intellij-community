package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestRunLineMarkerContributorTest : BasePlatformTestCase() {

    private val contributor = GoTestRunLineMarkerContributor()

    // --- non-test files always return null ---

    fun testGetInfo_elementInNonTestFile_returnsNull() {
        myFixture.configureByText("main.go", "package <caret>main\n\nfunc Foo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        assertNull(contributor.getInfo(element))
    }

    fun testGetInfo_elementInGoModFile_returnsNull() {
        myFixture.configureByText("go.mod", "module example.com/m\n\ngo 1.21")
        val element = myFixture.file.findElementAt(0) ?: return
        assertNull(contributor.getInfo(element))
    }

    // --- test file but element on non-special position ---

    fun testGetInfo_elementInsideFunctionBody_returnsNull() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc TestFoo() {\n  <caret>\n}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        assertNull(contributor.getInfo(element))
    }

    // --- package-level gutter icon ---

    fun testGetInfo_packageNameElement_withTestFunctions_returnsInfo() {
        myFixture.configureByText("main_test.go", "package <caret>main\n\nfunc TestFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val info = contributor.getInfo(element)
        assertNotNull("Package-level element with test functions should get a gutter icon", info)
    }

    fun testGetInfo_packageNameElement_noTestFunctions_returnsNull() {
        myFixture.configureByText("main_test.go", "package <caret>main\n\nfunc helper() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        assertNull(contributor.getInfo(element))
    }

    fun testGetInfo_packageNameElement_withBenchmarkFunctions_returnsInfo() {
        myFixture.configureByText("main_test.go", "package <caret>main\n\nfunc BenchmarkFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val info = contributor.getInfo(element)
        assertNotNull(info)
    }

    // --- function-level gutter icons ---

    fun testGetInfo_testFunctionName_returnsInfo() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>TestFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val info = contributor.getInfo(element)
        assertNotNull("TestFoo identifier should get a gutter icon", info)
    }

    fun testGetInfo_benchmarkFunctionName_returnsInfo() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>BenchmarkFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val info = contributor.getInfo(element)
        assertNotNull("BenchmarkFoo identifier should get a gutter icon", info)
    }

    fun testGetInfo_fuzzFunctionName_returnsInfo() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>FuzzFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val info = contributor.getInfo(element)
        assertNotNull("FuzzFoo identifier should get a gutter icon", info)
    }

    fun testGetInfo_testMainFunctionName_returnsNull() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>TestMain() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        assertNull("TestMain should not get a gutter icon", contributor.getInfo(element))
    }

    fun testGetInfo_nonTestFunctionName_returnsNull() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>helperFunc() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        assertNull(contributor.getInfo(element))
    }

    // --- passed/failed/skipped status changes icon ---

    fun testGetInfo_testFunctionWithPassedStatus_returnsInfo() {
        GoTestResultsService.getInstance(project).record("TestPassed", GoTestResultsService.Status.PASSED)
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>TestPassed() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        assertNotNull(contributor.getInfo(element))
    }

    fun testGetInfo_testFunctionWithFailedStatus_returnsInfo() {
        GoTestResultsService.getInstance(project).record("TestFailed", GoTestResultsService.Status.FAILED)
        myFixture.configureByText("main_test.go", "package main\n\nfunc <caret>TestFailed() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        assertNotNull(contributor.getInfo(element))
    }
}
