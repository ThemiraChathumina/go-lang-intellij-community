package com.github.themirachathumina.golangintellijcommunity.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoRunAnalyzerTest : BasePlatformTestCase() {

    fun testIsMainPackage() {
        val file = myFixture.configureByText("main.go", "package main\n\nfunc main() {}")
        assertTrue(GoRunAnalyzer.isMainPackage(file))
    }

    fun testIsNotMainPackage() {
        val file = myFixture.configureByText("lib.go", "package lib\n\nfunc Add() {}")
        assertFalse(GoRunAnalyzer.isMainPackage(file))
    }

    fun testHasFuncMain() {
        val file = myFixture.configureByText("main.go", "package main\n\nfunc main() {}")
        assertTrue(GoRunAnalyzer.hasFuncMain(file))
    }

    fun testDoesNotHaveFuncMain() {
        val file = myFixture.configureByText("main.go", "package main\n\nfunc init() {}")
        assertFalse(GoRunAnalyzer.hasFuncMain(file))
    }
    
    fun testNotAGoFile() {
        val file = myFixture.configureByText("test.txt", "package main\n\nfunc main() {}")
        assertFalse(GoRunAnalyzer.isMainPackage(file))
        assertFalse(GoRunAnalyzer.hasFuncMain(file))
    }
}
