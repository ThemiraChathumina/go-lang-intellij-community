package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestFinderExtendedTest : BasePlatformTestCase() {

    // --- findTestsForClass ---

    fun testFindTestsForClass_forFunctionDecl_findsMatchingTest() {
        val sourceFile = myFixture.addFileToProject("main.go", "package main\n\nfunc Foo() {}")
        myFixture.addFileToProject("main_test.go", "package main\n\nfunc TestFoo() {}")

        val fooFn = PsiTreeUtil.findChildrenOfType(sourceFile, GoFunctionDecl::class.java)
            .firstOrNull { it.functionName?.identifier?.text == "Foo" } ?: return

        val finder = GoTestFinder()
        val results = finder.findTestsForClass(fooFn)

        assertTrue(results.isNotEmpty())
    }

    fun testFindTestsForClass_forFunctionWithNoMatchingTest_returnsTestFile() {
        val sourceFile = myFixture.addFileToProject("util.go", "package main\n\nfunc Bar() {}")
        myFixture.addFileToProject("util_test.go", "package main\n\nfunc TestOther() {}")

        val fn = PsiTreeUtil.findChildrenOfType(sourceFile, GoFunctionDecl::class.java)
            .firstOrNull { it.functionName?.identifier?.text == "Bar" } ?: return

        val finder = GoTestFinder()
        val results = finder.findTestsForClass(fn)

        // No TestBar exists, so returns the test file
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is GoFile)
    }

    fun testFindTestsForClass_forTestFile_returnsEmpty() {
        val testFile = myFixture.configureByText("main_test.go", "package main\n\nfunc TestFoo() {}")
        val fn = PsiTreeUtil.findChildrenOfType(testFile, GoFunctionDecl::class.java).first()

        val finder = GoTestFinder()
        val results = finder.findTestsForClass(fn)

        assertTrue(results.isEmpty())
    }

    fun testFindTestsForClass_withNoTestFile_returnsEmpty() {
        // Source file exists but no corresponding test file
        val sourceFile = myFixture.configureByText("standalone.go", "package main\n\nfunc Standalone() {}")
        val fn = PsiTreeUtil.findChildrenOfType(sourceFile, GoFunctionDecl::class.java).first()

        val finder = GoTestFinder()
        val results = finder.findTestsForClass(fn)

        // No standalone_test.go → returns empty
        assertTrue(results.isEmpty())
    }

    // --- findClassesForTest ---

    fun testFindClassesForTest_forTestFunction_findsMatchingSourceFunction() {
        myFixture.addFileToProject("service.go", "package main\n\nfunc Add() {}")
        val testFile = myFixture.addFileToProject("service_test.go", "package main\n\nfunc TestAdd() {}")

        val testFn = PsiTreeUtil.findChildrenOfType(testFile, GoFunctionDecl::class.java)
            .firstOrNull { it.functionName?.identifier?.text == "TestAdd" } ?: return

        val finder = GoTestFinder()
        val results = finder.findClassesForTest(testFn)

        assertTrue(results.isNotEmpty())
    }

    fun testFindClassesForTest_forTestFunctionWithNoSourceMatch_returnsSourceFile() {
        myFixture.addFileToProject("calc.go", "package main\n\nfunc Multiply() {}")
        val testFile = myFixture.addFileToProject("calc_test.go", "package main\n\nfunc TestDivide() {}")

        val testFn = PsiTreeUtil.findChildrenOfType(testFile, GoFunctionDecl::class.java)
            .firstOrNull { it.functionName?.identifier?.text == "TestDivide" } ?: return

        val finder = GoTestFinder()
        val results = finder.findClassesForTest(testFn)

        // No Divide() in calc.go → returns calc.go (GoFile)
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is GoFile)
    }

    fun testFindClassesForTest_forFuzzFunction_returnsSourceFile() {
        myFixture.addFileToProject("parser.go", "package main\n\nfunc Parse() {}")
        val testFile = myFixture.addFileToProject("parser_test.go", "package main\n\nfunc FuzzParse() {}")

        val fuzzFn = PsiTreeUtil.findChildrenOfType(testFile, GoFunctionDecl::class.java)
            .firstOrNull { it.functionName?.identifier?.text == "FuzzParse" } ?: return

        val finder = GoTestFinder()
        val results = finder.findClassesForTest(fuzzFn)

        // FuzzParse doesn't start with "Test" → no source name → returns source file
        assertTrue(results.isNotEmpty())
        assertTrue(results.first() is GoFile)
    }

    fun testFindClassesForTest_forElementInSourceFile_returnsEmpty() {
        // findClassesForTest requires element to be in a _test.go file
        val sourceFile = myFixture.configureByText("helpers.go", "package main\n\nfunc Helper() {}")
        val fn = PsiTreeUtil.findChildrenOfType(sourceFile, GoFunctionDecl::class.java).first()

        val finder = GoTestFinder()
        val results = finder.findClassesForTest(fn)

        assertTrue(results.isEmpty())
    }

    fun testFindClassesForTest_withNoSourceFile_returnsEmpty() {
        // Test file exists but no corresponding source file
        val testFile = myFixture.configureByText("orphan_test.go", "package main\n\nfunc TestOrphan() {}")
        val fn = PsiTreeUtil.findChildrenOfType(testFile, GoFunctionDecl::class.java).first()

        val finder = GoTestFinder()
        val results = finder.findClassesForTest(fn)

        // No orphan.go → returns empty
        assertTrue(results.isEmpty())
    }
}
