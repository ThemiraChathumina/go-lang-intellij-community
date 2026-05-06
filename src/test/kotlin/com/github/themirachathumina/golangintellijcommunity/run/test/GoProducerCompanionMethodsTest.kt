package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoProducerCompanionMethodsTest : BasePlatformTestCase() {

    private fun firstFunction(code: String): GoFunctionDecl {
        val file = myFixture.configureByText("main_test.go", "package main\n\n$code")
        return PsiTreeUtil.findChildrenOfType(file, GoFunctionDecl::class.java).first()
    }

    // --- GoBenchmarkRunConfigurationProducer.isBenchmarkFunction ---

    fun testIsBenchmarkFunction_returnsTrue_forBenchmarkPrefix() {
        assertTrue(GoBenchmarkRunConfigurationProducer.isBenchmarkFunction(firstFunction("func BenchmarkFoo() {}")))
    }

    fun testIsBenchmarkFunction_returnsFalse_forTestPrefix() {
        assertFalse(GoBenchmarkRunConfigurationProducer.isBenchmarkFunction(firstFunction("func TestFoo() {}")))
    }

    fun testIsBenchmarkFunction_returnsFalse_forFuzzPrefix() {
        assertFalse(GoBenchmarkRunConfigurationProducer.isBenchmarkFunction(firstFunction("func FuzzFoo() {}")))
    }

    fun testIsBenchmarkFunction_returnsFalse_forOtherFunction() {
        assertFalse(GoBenchmarkRunConfigurationProducer.isBenchmarkFunction(firstFunction("func helperFunc() {}")))
    }

    fun testIsBenchmarkFunction_returnsFalse_forExamplePrefix() {
        assertFalse(GoBenchmarkRunConfigurationProducer.isBenchmarkFunction(firstFunction("func ExampleFoo() {}")))
    }

    // --- GoFuzzRunConfigurationProducer.isFuzzFunction ---

    fun testIsFuzzFunction_returnsTrue_forFuzzPrefix() {
        assertTrue(GoFuzzRunConfigurationProducer.isFuzzFunction(firstFunction("func FuzzReverse() {}")))
    }

    fun testIsFuzzFunction_returnsFalse_forTestPrefix() {
        assertFalse(GoFuzzRunConfigurationProducer.isFuzzFunction(firstFunction("func TestFoo() {}")))
    }

    fun testIsFuzzFunction_returnsFalse_forBenchmarkPrefix() {
        assertFalse(GoFuzzRunConfigurationProducer.isFuzzFunction(firstFunction("func BenchmarkFoo() {}")))
    }

    fun testIsFuzzFunction_returnsFalse_forOtherFunction() {
        assertFalse(GoFuzzRunConfigurationProducer.isFuzzFunction(firstFunction("func myFunc() {}")))
    }

    // --- GoTestRunConfigurationProducer.isTestFunction ---

    fun testIsTestFunction_returnsTrue_forTestPrefix() {
        assertTrue(GoTestRunConfigurationProducer.isTestFunction(firstFunction("func TestFoo() {}")))
    }

    fun testIsTestFunction_returnsTrue_forFuzzPrefix() {
        assertTrue(GoTestRunConfigurationProducer.isTestFunction(firstFunction("func FuzzFoo() {}")))
    }

    fun testIsTestFunction_returnsTrue_forExamplePrefix() {
        assertTrue(GoTestRunConfigurationProducer.isTestFunction(firstFunction("func ExampleFoo() {}")))
    }

    fun testIsTestFunction_returnsFalse_forBenchmarkPrefix() {
        assertFalse(GoTestRunConfigurationProducer.isTestFunction(firstFunction("func BenchmarkFoo() {}")))
    }

    fun testIsTestFunction_returnsFalse_forOtherFunction() {
        assertFalse(GoTestRunConfigurationProducer.isTestFunction(firstFunction("func myHelper() {}")))
    }

    fun testIsTestFunction_returnsTrue_forTestMain() {
        assertTrue(GoTestRunConfigurationProducer.isTestFunction(firstFunction("func TestMain() {}")))
    }

    // --- GoTestProducerGate.shouldSuppress (PSI-based) ---

    fun testShouldSuppress_insideFuzzFunction_returnsTrue() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc FuzzFoo() {\n  <caret>\n}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val goFile = myFixture.file as GoFile
        assertTrue(GoTestProducerGate.shouldSuppress(goFile, element))
    }

    fun testShouldSuppress_insideTestFunction_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc TestFoo() {\n  <caret>\n}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val goFile = myFixture.file as GoFile
        assertFalse(GoTestProducerGate.shouldSuppress(goFile, element))
    }

    fun testShouldSuppress_insideBenchmarkFunction_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc BenchmarkFoo() {\n  <caret>\n}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val goFile = myFixture.file as GoFile
        assertFalse(GoTestProducerGate.shouldSuppress(goFile, element))
    }

    fun testShouldSuppress_fileLevelWithTestsAndBenchmarks_returnsTrue() {
        myFixture.configureByText("main_test.go", "package <caret>main\n\nfunc TestFoo() {}\nfunc BenchmarkFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val goFile = myFixture.file as GoFile
        assertTrue(GoTestProducerGate.shouldSuppress(goFile, element))
    }

    fun testShouldSuppress_fileLevelWithFuzzFunctions_returnsTrue() {
        myFixture.configureByText("main_test.go", "package <caret>main\n\nfunc FuzzFoo() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val goFile = myFixture.file as GoFile
        assertTrue(GoTestProducerGate.shouldSuppress(goFile, element))
    }

    fun testShouldSuppress_fileLevelWithOnlyTests_returnsFalse() {
        myFixture.configureByText("main_test.go", "package <caret>main\n\nfunc TestFoo() {}\nfunc TestBar() {}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val goFile = myFixture.file as GoFile
        assertFalse(GoTestProducerGate.shouldSuppress(goFile, element))
    }

    fun testShouldSuppress_whenBypassed_returnsFalse() {
        myFixture.configureByText("main_test.go", "package main\n\nfunc FuzzFoo() {\n  <caret>\n}")
        val element = myFixture.file.findElementAt(myFixture.editor.caretModel.offset) ?: return
        val goFile = myFixture.file as GoFile
        val result = GoTestProducerGate.withBypass { GoTestProducerGate.shouldSuppress(goFile, element) }
        assertFalse(result)
    }

    // --- GoTestProducerGate.getTestFunctionsInDirectory ---

    fun testGetTestFunctionsInDirectory_findsAllFunctionsInTestFile() {
        val testFile = myFixture.addFileToProject(
            "pkg/funcs_test.go",
            "package pkg\n\nfunc TestAlpha() {}\nfunc BenchmarkBeta() {}"
        )
        val dir = testFile.containingDirectory
        val functions = GoTestProducerGate.getTestFunctionsInDirectory(dir)
        assertEquals(2, functions.size)
        val names = functions.map { it.functionName?.identifier?.text }
        assertTrue("TestAlpha" in names)
        assertTrue("BenchmarkBeta" in names)
    }

    fun testGetTestFunctionsInDirectory_ignoresNonTestGoFiles() {
        myFixture.addFileToProject("pkg2/main.go", "package pkg2\n\nfunc helperFunc() {}")
        val testFile = myFixture.addFileToProject("pkg2/main_test.go", "package pkg2\n\nfunc TestFoo() {}")
        val dir = testFile.containingDirectory
        val functions = GoTestProducerGate.getTestFunctionsInDirectory(dir)
        assertEquals(1, functions.size)
        assertEquals("TestFoo", functions[0].functionName?.identifier?.text)
    }

    fun testGetTestFunctionsInDirectory_emptyDir_returnsEmpty() {
        val nonTestFile = myFixture.addFileToProject("empty/main.go", "package empty")
        val dir = nonTestFile.containingDirectory
        val functions = GoTestProducerGate.getTestFunctionsInDirectory(dir)
        assertTrue(functions.isEmpty())
    }

    // --- GoTestProducerGate.shouldSuppressDirectory ---

    fun testShouldSuppressDirectory_withFuzzFunctions_returnsTrue() {
        val file = myFixture.addFileToProject("fuzzdir/fuzz_test.go", "package fuzzdir\n\nfunc FuzzFoo() {}")
        val dir = file.containingDirectory
        assertTrue(GoTestProducerGate.shouldSuppressDirectory(dir))
    }

    fun testShouldSuppressDirectory_withTestsAndBenchmarks_returnsTrue() {
        val file = myFixture.addFileToProject("mixdir/mix_test.go",
            "package mixdir\n\nfunc TestFoo() {}\nfunc BenchmarkFoo() {}")
        val dir = file.containingDirectory
        assertTrue(GoTestProducerGate.shouldSuppressDirectory(dir))
    }

    fun testShouldSuppressDirectory_withOnlyTests_returnsFalse() {
        val file = myFixture.addFileToProject("testonly/test_test.go",
            "package testonly\n\nfunc TestA() {}\nfunc TestB() {}")
        val dir = file.containingDirectory
        assertFalse(GoTestProducerGate.shouldSuppressDirectory(dir))
    }

    fun testShouldSuppressDirectory_whenBypassed_returnsFalse() {
        val file = myFixture.addFileToProject("bypassdir/mix_test.go",
            "package bypassdir\n\nfunc FuzzFoo() {}")
        val dir = file.containingDirectory
        val result = GoTestProducerGate.withBypass { GoTestProducerGate.shouldSuppressDirectory(dir) }
        assertFalse(result)
    }
}
