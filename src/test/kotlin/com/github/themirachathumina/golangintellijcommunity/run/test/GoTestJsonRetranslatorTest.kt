package com.github.themirachathumina.golangintellijcommunity.run.test

import org.junit.Assert.*
import org.junit.Test

class GoTestJsonRetranslatorTest {

    @Test
    fun testTranslateLine_InvalidJson_ReturnsOriginal() {
        val retranslator = GoTestJsonRetranslator()
        val input = "not a json string"
        val result = retranslator.translateLine(input)
        assertEquals(input, result)
    }

    @Test
    fun testTranslateLine_PassEvent() {
        var recordedTestName: String? = null
        var recordedPassed: Boolean? = null
        val retranslator = GoTestJsonRetranslator { name, passed ->
            recordedTestName = name
            recordedPassed = passed
        }

        // Output first so the retranslator knows it's pending
        val runJson = """{"Time":"2024-01-01T12:00:00.000Z","Action":"run","Package":"pkg","Test":"TestFoo"}"""
        retranslator.translateLine(runJson)

        val passJson = """{"Time":"2024-01-01T12:00:01.000Z","Action":"pass","Package":"pkg","Test":"TestFoo","Elapsed":1.0}"""
        val result = retranslator.translateLine(passJson)

        assertNotNull("Result should not be null", result)
        assertTrue("Should contain testFinished", result!!.contains("testFinished"))
        assertTrue("Should contain TestFoo", result.contains("TestFoo"))
        assertEquals("TestFoo", recordedTestName)
        assertEquals(true, recordedPassed)
    }

    @Test
    fun testTranslateLine_FailEvent() {
        var recordedTestName: String? = null
        var recordedPassed: Boolean? = null
        val retranslator = GoTestJsonRetranslator { name, passed ->
            recordedTestName = name
            recordedPassed = passed
        }

        retranslator.translateLine("""{"Time":"2024-01-01T12:00:00.000Z","Action":"run","Package":"pkg","Test":"TestBar"}""")
        val failJson = """{"Time":"2024-01-01T12:00:01.000Z","Action":"fail","Package":"pkg","Test":"TestBar","Elapsed":1.0}"""
        val result = retranslator.translateLine(failJson)

        assertNotNull(result)
        assertTrue(result!!.contains("testFailed"))
        assertEquals("TestBar", recordedTestName)
        assertEquals(false, recordedPassed)
    }

    @Test
    fun testTranslateLine_SkipEvent() {
        var recordedTestName: String? = null
        var recordedPassed: Boolean? = null
        val retranslator = GoTestJsonRetranslator { name, passed ->
            recordedTestName = name
            recordedPassed = passed
        }

        retranslator.translateLine("""{"Time":"2024-01-01T12:00:00.000Z","Action":"run","Package":"pkg","Test":"TestSkip"}""")
        val skipJson = """{"Time":"2024-01-01T12:00:01.000Z","Action":"skip","Package":"pkg","Test":"TestSkip","Elapsed":0.0}"""
        val result = retranslator.translateLine(skipJson)

        assertNotNull(result)
        assertTrue(result!!.contains("testIgnored"))
        assertEquals("TestSkip", recordedTestName)
        assertNull(recordedPassed) // skip is null
    }

    @Test
    fun testTranslateLine_OutputEvent() {
        val retranslator = GoTestJsonRetranslator()
        
        retranslator.translateLine("""{"Time":"2024-01-01T12:00:00.000Z","Action":"run","Package":"pkg","Test":"TestOut"}""")
        
        val outputJson = """{"Time":"2024-01-01T12:00:00.500Z","Action":"output","Package":"pkg","Test":"TestOut","Output":"some output\n"}"""
        val result = retranslator.translateLine(outputJson)
        
        // Output for pending tests is buffered until completion
        assertNull(result)

        val passJson = """{"Time":"2024-01-01T12:00:01.000Z","Action":"pass","Package":"pkg","Test":"TestOut","Elapsed":1.0}"""
        val finalResult = retranslator.translateLine(passJson)
        
        assertNotNull(finalResult)
        assertTrue(finalResult!!.contains("testStdOut"))
        assertTrue(finalResult.contains("some output|n"))
    }
}
