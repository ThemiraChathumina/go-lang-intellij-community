package com.github.themirachathumina.golangintellijcommunity.ide.actions

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.util.Locale

class GoCreateFileAction : CreateFileFromTemplateAction(
    "Go File",
    "Create a new Go file",
    GoIcons.FILE
) {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("New Go File")
            .addKind("Simple File", GoIcons.FILE, "Go File")
            .addKind("Main File", GoIcons.FILE, "Go Main File")
            .addKind("Test File", GoIcons.FILE, "Go Test File")
    }

    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
        return "Create Go File: $newName"
    }

    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        val project = dir.project

        val baseName = name.removeSuffix(".go")
        var fileName = "$baseName.go"

        // Calculate dynamic package name
        var pkgName = dir.name.replace(Regex("[^a-zA-Z0-9_]"), "")
        if (pkgName.isEmpty() || pkgName.first().isDigit()) {
            pkgName = "pkg$pkgName"
        }

        val content: String

        when (templateName) {
            "Go Main File" -> {
                content = "package main\n\nimport \"fmt\"\n\nfunc main() {\n\tfmt.Println(\"Hello, World!\")\n}\n"
            }
            "Go Test File" -> {
                if (!fileName.endsWith("_test.go")) {
                    fileName = "${baseName}_test.go"
                }
                val testFuncName = baseName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
                content = "package $pkgName\n\nimport \"testing\"\n\nfunc Test$testFuncName(t *testing.T) {\n\t// TODO: Initialize test\n}\n"
            }
            else -> {
                content = "package $pkgName\n"
            }
        }

        return WriteAction.compute<PsiFile?, RuntimeException> {
            val virtualDir = dir.virtualFile

            // 1. Create the file with a dummy extension so gopls ignores it initially
            val tempFileName = "$fileName.tmp"
            val virtualFile = virtualDir.createChildData(this, tempFileName)

            // 2. Atomically inject the text
            virtualFile.setBinaryContent(content.toByteArray(Charsets.UTF_8))

            // 3. Rename it to the actual .go file name.
            // lsp4ij is now notified, but the file already has content, so gopls does nothing.
            virtualFile.rename(this, fileName)

            // 4. Convert back to PSI so the IDE opens it in the editor
            PsiManager.getInstance(project).findFile(virtualFile)
        }
    }
}