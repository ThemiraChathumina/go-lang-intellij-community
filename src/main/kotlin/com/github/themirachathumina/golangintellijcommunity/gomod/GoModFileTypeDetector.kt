package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile

class GoModFileTypeDetector : FileTypeRegistry.FileTypeDetector {
    override fun detect(file: VirtualFile, firstBytes: ByteSequence, firstCharsIfText: CharSequence?): FileType? {
        return when (file.name) {
            "go.mod", "go.work", "go.sum" -> GoModFileType
            else -> null
        }
    }

    override fun getDesiredContentPrefixLength(): Int = 0
}
