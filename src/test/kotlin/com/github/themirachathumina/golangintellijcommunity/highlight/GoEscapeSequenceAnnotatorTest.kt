package com.github.themirachathumina.golangintellijcommunity.highlight

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoEscapeSequenceAnnotatorTest : BasePlatformTestCase() {

    fun testEscapeSequences() {
        myFixture.configureByText("main.go", """
            package main
            fun main() {
                val s = "valid escapes: \n \t \\ \" \x61 \u1234 \U00001234"
                val r = '\n'
                val raw = `not an escape: \n`
            }
        """.trimIndent())
        
        // Similarly to FormatVerbAnnotator, we could use checkHighlighting() 
        // if we had a way to assert on specific TextAttributes.
    }
}
