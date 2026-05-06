package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoTestContextGroupsTest : BasePlatformTestCase() {

    // --- GoTestRunContextGroup ---

    fun testRunContextGroup_isPopup() {
        val group = GoTestRunContextGroup()
        assertTrue(group.isPopup)
    }

    fun testRunContextGroup_templateText() {
        val group = GoTestRunContextGroup()
        assertEquals("Run", group.templatePresentation.text)
    }

    fun testRunContextGroup_templateIcon() {
        val group = GoTestRunContextGroup()
        assertEquals(AllIcons.Actions.Execute, group.templatePresentation.icon)
    }

    fun testRunContextGroup_actionUpdateThread_isBGT() {
        val group = GoTestRunContextGroup()
        assertEquals(ActionUpdateThread.BGT, group.getActionUpdateThread())
    }

    fun testRunContextGroup_getChildren_withNoContext_returnsEmptyOrValid() {
        val group = GoTestRunContextGroup()
        // getChildren reads cached data; before any update() call, cachedConfigs is empty
        val children = group.getChildren(null)
        assertNotNull(children)
        // With no cached fuzz info and empty cached configs, children should be empty
        assertEquals(0, children.size)
    }

    fun testRunContextGroup_update_withNoTestContext_isNotVisible() {
        val group = GoTestRunContextGroup()
        // Create an event with no PSI context (empty data context)
        val dataContext = SimpleDataContext.getProjectContext(project)
        val event = AnActionEvent.createFromDataContext("test", null, dataContext)
        group.update(event)
        // Without a test file element, the group resolves no matching configs
        assertFalse(event.presentation.isEnabledAndVisible)
    }

    // --- GoTestModifyRunContextGroup ---

    fun testModifyRunContextGroup_isPopup() {
        val group = GoTestModifyRunContextGroup()
        assertTrue(group.isPopup)
    }

    fun testModifyRunContextGroup_templateText() {
        val group = GoTestModifyRunContextGroup()
        assertEquals("Modify Run Configuration", group.templatePresentation.text)
    }

    fun testModifyRunContextGroup_templateIcon() {
        val group = GoTestModifyRunContextGroup()
        assertEquals(AllIcons.Actions.EditSource, group.templatePresentation.icon)
    }

    fun testModifyRunContextGroup_actionUpdateThread_isBGT() {
        val group = GoTestModifyRunContextGroup()
        assertEquals(ActionUpdateThread.BGT, group.getActionUpdateThread())
    }

    fun testModifyRunContextGroup_getChildren_withNoContext_returnsEmpty() {
        val group = GoTestModifyRunContextGroup()
        val children = group.getChildren(null)
        assertNotNull(children)
        assertEquals(0, children.size)
    }

    fun testModifyRunContextGroup_update_withNoTestContext_isNotVisible() {
        val group = GoTestModifyRunContextGroup()
        val dataContext = SimpleDataContext.getProjectContext(project)
        val event = AnActionEvent.createFromDataContext("test", null, dataContext)
        group.update(event)
        assertFalse(event.presentation.isEnabledAndVisible)
    }
}
