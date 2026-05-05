package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class GoTestFinderTest : BasePlatformTestCase() {

    fun testIsTest() {
        val testFile = myFixture.configureByText("main_test.go", "package main")
        val sourceFile = myFixture.configureByText("main.go", "package main")
        
        val finder = GoTestFinder()
        
        assertTrue(finder.isTest(testFile))
        assertFalse(finder.isTest(sourceFile))
    }

    fun testFindSourceElement() {
        val file = myFixture.configureByText("main_test.go", "package main\n\nfunc TestFoo() {\n  <caret>\n}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)
        
        val finder = GoTestFinder()
        val sourceElement = finder.findSourceElement(element!!)
        
        assertNotNull(sourceElement)
        // Should find the GoFunctionDecl
        assertTrue(sourceElement!!.text.startsWith("func TestFoo"))
    }
}
