package com.github.themirachathumina.golangintellijcommunity.run.test

import org.junit.Assert.*
import org.junit.Test

class GoTestProducerGateTest {

    @Test
    fun testIsBypassed_defaultsFalse() {
        assertFalse(GoTestProducerGate.isBypassed())
    }

    @Test
    fun testWithBypass_isBypassedInsideBlock() {
        var insideBypass = false
        GoTestProducerGate.withBypass {
            insideBypass = GoTestProducerGate.isBypassed()
        }
        assertTrue(insideBypass)
    }

    @Test
    fun testWithBypass_restoresFalseAfterBlock() {
        GoTestProducerGate.withBypass { /* no-op */ }
        assertFalse(GoTestProducerGate.isBypassed())
    }

    @Test
    fun testWithBypass_returnsBlockResult() {
        val result = GoTestProducerGate.withBypass { 42 }
        assertEquals(42, result)
    }

    @Test
    fun testWithBypass_restoresFalseOnException() {
        try {
            GoTestProducerGate.withBypass { error("test exception") }
        } catch (_: IllegalStateException) {}
        assertFalse(GoTestProducerGate.isBypassed())
    }

    @Test
    fun testWithBypass_nestedRestoredCorrectly() {
        var innerValue = false
        GoTestProducerGate.withBypass {
            GoTestProducerGate.withBypass {
                innerValue = GoTestProducerGate.isBypassed()
            }
            assertTrue("bypass should still be true after inner block", GoTestProducerGate.isBypassed())
        }
        assertTrue(innerValue)
        assertFalse(GoTestProducerGate.isBypassed())
    }

    @Test
    fun testWithBypass_returnsStringResult() {
        val result = GoTestProducerGate.withBypass { "hello" }
        assertEquals("hello", result)
    }

    @Test
    fun testWithBypass_returnsNullResult() {
        val result: String? = GoTestProducerGate.withBypass { null }
        assertNull(result)
    }
}
