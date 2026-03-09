package com.github.themirachathumina.golangintellijcommunity

import com.intellij.testFramework.ParsingTestCase

class GoParserTest : ParsingTestCase(
    "parser",  // 1. Tells the runner to look inside the "parser" subfolder
    "go",      // 2. MUST be lowercase to match your .go files
    GoParserDefinition()
) {

    override fun getTestDataPath(): String {
        // Provide the absolute path to ensure Gradle finds it regardless of the working directory
        return "src/test/testData"
    }

    override fun skipSpaces(): Boolean {
        return true
    }

    override fun includeRanges(): Boolean {
        return true
    }

    fun testHelloWorld() = doTest(true)
    fun testVariables() = doTest(true)
    fun testFunctions() = doTest(true)
    fun testAsi() = doTest(true)
    fun testAsi2() = doTest(true)
    fun testControlFlow() = doTest(true)
    fun testControlFlow2() = doTest(true)
    fun testStructs() = doTest(true)
    fun testConcurrency() = doTest(true)
    fun testDataStructures() = doTest(true)
    fun testDeclarations() = doTest(true)
    fun testMethods() = doTest(true)
    fun testTypes() = doTest(true)
    fun testComplexChan() = doTest(true)
    fun testMethodExpressions() = doTest(true)
    fun testLabels() = doTest(true)
    fun testVariadics() = doTest(true)
    fun testEmptyBlocks() = doTest(true)
    fun testIfElse() = doTest(true)
}