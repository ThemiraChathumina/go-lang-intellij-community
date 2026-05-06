package com.github.themirachathumina.golangintellijcommunity.run.test

import org.junit.Assert.*
import org.junit.Test

class GoTestResultsServiceTest {

    @Test
    fun testRecord_andGetStatus_returnsRecordedStatus() {
        val service = GoTestResultsService()
        service.record("TestFoo", GoTestResultsService.Status.PASSED)
        assertEquals(GoTestResultsService.Status.PASSED, service.getStatus("TestFoo"))
    }

    @Test
    fun testGetStatus_unknownTest_returnsNull() {
        val service = GoTestResultsService()
        assertNull(service.getStatus("TestUnknown"))
    }

    @Test
    fun testRecord_overwritesPreviousStatus() {
        val service = GoTestResultsService()
        service.record("TestFoo", GoTestResultsService.Status.PASSED)
        service.record("TestFoo", GoTestResultsService.Status.FAILED)
        assertEquals(GoTestResultsService.Status.FAILED, service.getStatus("TestFoo"))
    }

    @Test
    fun testRecord_allStatusValues() {
        val service = GoTestResultsService()
        service.record("A", GoTestResultsService.Status.PASSED)
        service.record("B", GoTestResultsService.Status.FAILED)
        service.record("C", GoTestResultsService.Status.SKIPPED)
        assertEquals(GoTestResultsService.Status.PASSED, service.getStatus("A"))
        assertEquals(GoTestResultsService.Status.FAILED, service.getStatus("B"))
        assertEquals(GoTestResultsService.Status.SKIPPED, service.getStatus("C"))
    }

    @Test
    fun testGetState_includesAllRecordedResults() {
        val service = GoTestResultsService()
        service.record("TestA", GoTestResultsService.Status.PASSED)
        service.record("TestB", GoTestResultsService.Status.FAILED)

        val state = service.getState()
        assertEquals(GoTestResultsService.Status.PASSED, state.testStatuses["TestA"])
        assertEquals(GoTestResultsService.Status.FAILED, state.testStatuses["TestB"])
    }

    @Test
    fun testGetState_emptyWhenNoResults() {
        val service = GoTestResultsService()
        val state = service.getState()
        assertTrue(state.testStatuses.isEmpty())
    }

    @Test
    fun testLoadState_restoresResults() {
        val service = GoTestResultsService()
        val state = GoTestResultsService.State()
        state.testStatuses["TestX"] = GoTestResultsService.Status.SKIPPED
        state.testStatuses["TestY"] = GoTestResultsService.Status.PASSED

        service.loadState(state)

        assertEquals(GoTestResultsService.Status.SKIPPED, service.getStatus("TestX"))
        assertEquals(GoTestResultsService.Status.PASSED, service.getStatus("TestY"))
    }

    @Test
    fun testLoadState_clearsOldResults() {
        val service = GoTestResultsService()
        service.record("TestOld", GoTestResultsService.Status.PASSED)
        service.loadState(GoTestResultsService.State())
        assertNull(service.getStatus("TestOld"))
    }

    @Test
    fun testGetState_afterLoadState_reflectsLoadedData() {
        val service = GoTestResultsService()
        val state = GoTestResultsService.State()
        state.testStatuses["TestLoaded"] = GoTestResultsService.Status.FAILED

        service.loadState(state)
        val retrievedState = service.getState()

        assertEquals(GoTestResultsService.Status.FAILED, retrievedState.testStatuses["TestLoaded"])
    }

    @Test
    fun testStatus_hasThreeValues() {
        val values = GoTestResultsService.Status.values()
        assertEquals(3, values.size)
        assertTrue(GoTestResultsService.Status.PASSED in values)
        assertTrue(GoTestResultsService.Status.FAILED in values)
        assertTrue(GoTestResultsService.Status.SKIPPED in values)
    }

    @Test
    fun testRecord_multipleTests_independentOfEachOther() {
        val service = GoTestResultsService()
        service.record("Test1", GoTestResultsService.Status.PASSED)
        service.record("Test2", GoTestResultsService.Status.FAILED)
        service.record("Test1", GoTestResultsService.Status.SKIPPED)

        assertEquals(GoTestResultsService.Status.SKIPPED, service.getStatus("Test1"))
        assertEquals(GoTestResultsService.Status.FAILED, service.getStatus("Test2"))
    }
}
