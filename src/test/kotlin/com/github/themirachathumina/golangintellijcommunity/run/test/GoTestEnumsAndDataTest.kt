package com.github.themirachathumina.golangintellijcommunity.run.test

import org.junit.Assert.*
import org.junit.Test

class GoTestEnumsAndDataTest {

    @Test
    fun testGoTestKind_allValuesPresent() {
        val values = GoTestKind.values()
        assertEquals(3, values.size)
        assertTrue(GoTestKind.TEST in values)
        assertTrue(GoTestKind.BENCHMARK in values)
        assertTrue(GoTestKind.FUZZ in values)
    }

    @Test
    fun testGoTestKind_ordinals() {
        assertEquals(0, GoTestKind.TEST.ordinal)
        assertEquals(1, GoTestKind.BENCHMARK.ordinal)
        assertEquals(2, GoTestKind.FUZZ.ordinal)
    }

    @Test
    fun testGoTestScope_allValuesPresent() {
        val values = GoTestScope.values()
        assertEquals(4, values.size)
        assertTrue(GoTestScope.DIRECTORY in values)
        assertTrue(GoTestScope.PACKAGE in values)
        assertTrue(GoTestScope.FILE in values)
        assertTrue(GoTestScope.FUNCTION in values)
    }

    @Test
    fun testGoTestScope_ordinals() {
        assertEquals(0, GoTestScope.DIRECTORY.ordinal)
        assertEquals(1, GoTestScope.PACKAGE.ordinal)
        assertEquals(2, GoTestScope.FILE.ordinal)
        assertEquals(3, GoTestScope.FUNCTION.ordinal)
    }

    @Test
    fun testGoTestJsonEvent_fieldsPopulated() {
        val event = GoTestJsonEvent(
            time = "2024-01-01T00:00:00Z",
            action = "pass",
            packageName = "pkg",
            testName = "TestFoo",
            elapsed = 1.5,
            output = "ok"
        )
        assertEquals("2024-01-01T00:00:00Z", event.time)
        assertEquals("pass", event.action)
        assertEquals("pkg", event.packageName)
        assertEquals("TestFoo", event.testName)
        assertEquals(1.5, event.elapsed)
        assertEquals("ok", event.output)
    }

    @Test
    fun testGoTestJsonEvent_defaultNullFields() {
        val event = GoTestJsonEvent(action = "run")
        assertEquals("run", event.action)
        assertNull(event.time)
        assertNull(event.packageName)
        assertNull(event.testName)
        assertNull(event.elapsed)
        assertNull(event.output)
    }

    @Test
    fun testGoTestJsonEvent_equality() {
        val a = GoTestJsonEvent(action = "pass", testName = "TestFoo")
        val b = GoTestJsonEvent(action = "pass", testName = "TestFoo")
        assertEquals(a, b)
        assertNotEquals(a, GoTestJsonEvent(action = "fail", testName = "TestFoo"))
        assertNotEquals(a, GoTestJsonEvent(action = "pass", testName = "TestBar"))
    }

    @Test
    fun testGoTestJsonEvent_copy() {
        val original = GoTestJsonEvent(action = "pass", testName = "TestFoo", elapsed = 0.5)
        val copy = original.copy(elapsed = 1.0)
        assertEquals("pass", copy.action)
        assertEquals("TestFoo", copy.testName)
        assertEquals(1.0, copy.elapsed)
        assertEquals(0.5, original.elapsed)
    }

    @Test
    fun testGoTestJsonEvent_hashCode_equalsObjects_sameHash() {
        val a = GoTestJsonEvent(action = "pass", testName = "TestFoo", elapsed = 1.0)
        val b = GoTestJsonEvent(action = "pass", testName = "TestFoo", elapsed = 1.0)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
