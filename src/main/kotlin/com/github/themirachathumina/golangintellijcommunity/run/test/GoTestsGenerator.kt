package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.sdk.GoProjectSettings
import com.github.themirachathumina.golangintellijcommunity.sdk.GoSdkLocator
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

/*
 * Executes gotests in a background task and navigates to the generated test file on completion.
 * Shows a balloon notification with an "Install gotests" button when the tool is not found.
 */
object GoTestsGenerator {

    private const val GOTESTS_INSTALL_PATH = "github.com/cweill/gotests/gotests@latest"

    fun generateForFunction(project: Project, funcName: String, sourceFile: VirtualFile) {
        runGotests(
            project,
            args = listOf("-w", "-only", "^$funcName$", sourceFile.path),
            navigateTo = testFilePathFor(sourceFile.path)
        )
    }

    fun generateForFile(project: Project, sourceFile: VirtualFile) {
        runGotests(
            project,
            args = listOf("-w", "-all", sourceFile.path),
            navigateTo = testFilePathFor(sourceFile.path)
        )
    }

    fun generateForDirectory(project: Project, directory: VirtualFile) {
        val sourceFiles = directory.children
            .filter { it.extension == GoConstants.FileExtensions.GO_EXT && !it.name.endsWith(GoConstants.FileExtensions.TEST_SUFFIX) }
            .sortedBy { it.name }

        if (sourceFiles.isEmpty()) {
            notify(project, "No Go source files found in directory.", NotificationType.WARNING)
            return
        }

        runGotests(
            project,
            args = listOf("-w", "-all") + sourceFiles.map { it.path },
            navigateTo = testFilePathFor(sourceFiles.first().path)
        )
    }

    private fun runGotests(project: Project, args: List<String>, navigateTo: String?) {
        val gotests = GoTestsToolLocator.findGotests()
        if (gotests == null) {
            showInstallNotification(project)
            return
        }

        // Flush unsaved editor changes to disk so gotests reads the current state of _test.go.
        // Without this, deleted tests remain on disk and gotests skips regenerating them.
        FileDocumentManager.getInstance().saveAllDocuments()

        val command = listOf(gotests) + args

        object : Task.Backgroundable(project, "Generating Go Tests", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running gotests…"
                indicator.isIndeterminate = true

                val (exitCode, output) = try {
                    val proc = ProcessBuilder(command).redirectErrorStream(true).start()
                    val out = proc.inputStream.bufferedReader().readText().trim()
                    proc.waitFor() to out
                } catch (ex: Exception) {
                    -1 to (ex.message ?: "unknown error")
                }

                ApplicationManager.getApplication().invokeLater {
                    if (exitCode == 0) {
                        if (navigateTo != null) {
                            val testVFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(navigateTo)
                            if (testVFile != null) {
                                FileEditorManager.getInstance(project).openFile(testVFile, true)
                            }
                        }
                        notify(project, "Tests generated successfully.", NotificationType.INFORMATION)
                    } else {
                        val detail = if (output.isNotBlank()) "\n$output" else ""
                        notify(project, "gotests failed (exit $exitCode).$detail", NotificationType.ERROR)
                    }
                }
            }
        }.queue()
    }

    private fun installGotests(project: Project) {
        val sdkPath = GoProjectSettings.getInstance(project).state.goSdkPath
        val goExe = GoSdkLocator.getGoExecutablePath(sdkPath)
        if (goExe == null) {
            notify(project, "Go SDK not configured — cannot install gotests.", NotificationType.WARNING)
            return
        }

        object : Task.Backgroundable(project, "Installing gotests", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running go install $GOTESTS_INSTALL_PATH…"
                indicator.isIndeterminate = true

                val (exitCode, output) = try {
                    val proc = ProcessBuilder(goExe, "install", GOTESTS_INSTALL_PATH)
                        .redirectErrorStream(true)
                        .start()
                    val out = proc.inputStream.bufferedReader().readText().trim()
                    proc.waitFor() to out
                } catch (ex: Exception) {
                    -1 to (ex.message ?: "unknown error")
                }

                ApplicationManager.getApplication().invokeLater {
                    if (exitCode == 0) {
                        notify(project, "gotests installed successfully.", NotificationType.INFORMATION)
                    } else {
                        val detail = if (output.isNotBlank()) "\n$output" else ""
                        notify(project, "Failed to install gotests (exit $exitCode).$detail", NotificationType.ERROR)
                    }
                }
            }
        }.queue()
    }

    private fun testFilePathFor(sourceFilePath: String): String =
        sourceFilePath.removeSuffix(GoConstants.FileExtensions.GO) + GoConstants.FileExtensions.TEST_SUFFIX

    private fun showInstallNotification(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Plugin Notifications")
            .createNotification("gotests not found. Install it to enable test generation.", NotificationType.WARNING)
            .addAction(NotificationAction.createSimple("Install gotests") { installGotests(project) })
            .notify(project)
    }

    private fun notify(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Go Plugin Notifications")
            .createNotification(message, type)
            .notify(project)
    }
}
