package com.github.themirachathumina.golangintellijcommunity.run.test

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoCleanTestCacheActionTest : BasePlatformTestCase() {

    private val capturedNotifications = mutableListOf<Notification>()

    override fun setUp() {
        super.setUp()
        capturedNotifications.clear()
        project.messageBus.connect(testRootDisposable)
            .subscribe(Notifications.TOPIC, object : Notifications {
                override fun notify(notification: Notification) {
                    capturedNotifications.add(notification)
                }
            })
    }

    // --- update() ---

    fun testUpdate_withProject_isEnabledAndVisible() {
        val action = GoCleanTestCacheAction()
        val dataContext = SimpleDataContext.getProjectContext(project)
        val event = AnActionEvent.createFromDataContext("test", null, dataContext)
        action.update(event)
        assertTrue(event.presentation.isEnabledAndVisible)
    }

    fun testUpdate_withoutProject_isNotEnabledAndVisible() {
        val action = GoCleanTestCacheAction()
        val dataContext = SimpleDataContext.EMPTY_CONTEXT
        val event = AnActionEvent.createFromDataContext("test", null, dataContext)
        action.update(event)
        assertFalse(event.presentation.isEnabledAndVisible)
    }

    // --- cleanTestCache(): no SDK configured ---

    fun testCleanTestCache_noSdkConfigured_sendsWarningNotification() {
        val action = GoCleanTestCacheAction(
            resolveGoExe = { null }
        )
        action.cleanTestCache(project)

        val warning = capturedNotifications.find { it.type == NotificationType.WARNING }
        assertNotNull("Expected a WARNING notification when SDK not configured", warning)
        assertTrue(warning!!.content.contains("Go SDK not configured"))
    }

    // --- cleanTestCache(): process exits successfully ---

    fun testCleanTestCache_processSucceeds_sendsInfoNotification() {
        val action = GoCleanTestCacheAction(
            resolveGoExe = { "/fake/go" },
            processLauncher = { _ -> 0 to "" }
        )
        action.cleanTestCache(project)

        val info = capturedNotifications.find { it.type == NotificationType.INFORMATION }
        assertNotNull("Expected an INFORMATION notification on success", info)
        assertTrue(info!!.content.contains("cleaned successfully"))
    }

    // --- cleanTestCache(): process exits with error ---

    fun testCleanTestCache_processFails_sendsErrorNotification() {
        val action = GoCleanTestCacheAction(
            resolveGoExe = { "/fake/go" },
            processLauncher = { _ -> 1 to "disk full" }
        )
        action.cleanTestCache(project)

        val error = capturedNotifications.find { it.type == NotificationType.ERROR }
        assertNotNull("Expected an ERROR notification on failure", error)
        assertTrue(error!!.content.contains("failed"))
        assertTrue(error.content.contains("exit 1"))
    }

    fun testCleanTestCache_processFails_withBlankOutput_noDetailInMessage() {
        val action = GoCleanTestCacheAction(
            resolveGoExe = { "/fake/go" },
            processLauncher = { _ -> 2 to "" }
        )
        action.cleanTestCache(project)

        val error = capturedNotifications.find { it.type == NotificationType.ERROR }
        assertNotNull(error)
        assertFalse("Blank output should not add extra detail", error!!.content.contains("\n"))
    }

    fun testCleanTestCache_processFails_withOutput_includesDetailInMessage() {
        val action = GoCleanTestCacheAction(
            resolveGoExe = { "/fake/go" },
            processLauncher = { _ -> 1 to "some detailed error" }
        )
        action.cleanTestCache(project)

        val error = capturedNotifications.find { it.type == NotificationType.ERROR }
        assertNotNull(error)
        assertTrue(error!!.content.contains("some detailed error"))
    }

    // --- processLauncher receives correct command ---

    fun testCleanTestCache_launcherReceivesCorrectCommand() {
        var capturedCommand: List<String>? = null
        val action = GoCleanTestCacheAction(
            resolveGoExe = { "/usr/local/go/bin/go" },
            processLauncher = { cmd ->
                capturedCommand = cmd
                0 to ""
            }
        )
        action.cleanTestCache(project)

        assertNotNull(capturedCommand)
        assertEquals("/usr/local/go/bin/go", capturedCommand!![0])
        assertEquals("clean", capturedCommand!![1])
        assertEquals("-testcache", capturedCommand!![2])
    }
}
