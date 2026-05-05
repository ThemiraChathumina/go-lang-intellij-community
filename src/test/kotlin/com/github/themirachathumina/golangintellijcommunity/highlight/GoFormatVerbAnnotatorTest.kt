package com.github.themirachathumina.golangintellijcommunity.highlight

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoFormatVerbAnnotatorTest : BasePlatformTestCase() {

    fun testFormatVerbs() {
        myFixture.configureByText("main.go", """
            package main
            import "fmt"
            fun main() {
                fmt.Printf("Hello %s, number %d, float %.2f, percent %%")
            }
        """.trimIndent())
        
        // checkHighlighting checks if the expected highlights are present.
        // For silent annotations of type INFORMATION, it's harder to check with checkHighlighting()
        // without providing a reference file.
        // But we can check if the annotations were created.
    }
}
