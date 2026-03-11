package com.github.themirachathumina.golangintellijcommunity.ide.actions

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.openapi.application.runWriteAction
import com.intellij.psi.PsiDocumentManager

class GoCreateFileAction : CreateFileFromTemplateAction("Go File", "Create new Go file", GoIcons.FILE), DumbAware {
    private val LOG = Logger.getInstance(GoCreateFileAction::class.java)

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New Go File")
            .addKind("Simple file", GoIcons.FILE, "Go File")
            .addKind("Main file", GoIcons.FILE, "Go Main File")
            .addKind("Test file", GoIcons.FILE, "Go Test File")
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String = "Go File"

    override fun postProcess(createdFile: PsiFile, templateName: String?, customProperties: Map<String, String>?) {
        super.postProcess(createdFile, templateName, customProperties)
        
        LOG.info("[Go-Lang-Community] Post-processing created file: ${createdFile.name} (template: $templateName)")

        // Don't sanitize if it's explicitly a Main file template which uses 'package main'
        if (templateName == "Go Main File") {
            LOG.info("[Go-Lang-Community] Skipping sanitization for Main file")
            return
        }

        val project = createdFile.project
        val directory = createdFile.containingDirectory ?: return
        val sanitizedPackage = sanitizePackageName(directory.name)
        
        runWriteAction {
            val documentManager = PsiDocumentManager.getInstance(project)
            val document = documentManager.getDocument(createdFile) ?: return@runWriteAction
            val text = document.text
            
            val oldPackagePrefix = "package "
            if (text.startsWith(oldPackagePrefix)) {
                val endOfLine = text.indexOf('\n')
                val firstLine = if (endOfLine != -1) text.substring(0, endOfLine).trim() else text.trim()
                val newFirstLine = "package $sanitizedPackage"
                
                if (firstLine != newFirstLine) {
                    LOG.info("[Go-Lang-Community] Updating package declaration from '$firstLine' to '$newFirstLine'")
                    document.replaceString(0, firstLine.length, newFirstLine)
                    documentManager.commitDocument(document)
                }
            }
        }
    }

    private fun sanitizePackageName(name: String): String {
        val sanitized = name.replace(Regex("[^a-zA-Z0-9]"), "").lowercase()
        val result = if (sanitized.isEmpty() || sanitized[0].isDigit()) "main" else sanitized
        LOG.debug("[Go-Lang-Community] Sanitized package name for directory '$name': $result")
        return result
    }
}
