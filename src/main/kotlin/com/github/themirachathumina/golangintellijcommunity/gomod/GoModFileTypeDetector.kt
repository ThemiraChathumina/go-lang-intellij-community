package com.github.themirachathumina.golangintellijcommunity.gomod

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile

class GoModFileTypeDetector : FileTypeRegistry.FileTypeDetector {
    override fun detect(file: VirtualFile, firstBytes: ByteSequence, firstCharsIfText: CharSequence?): FileType? {
        return when (file.name) {
            GoConstants.FileExtensions.MOD, GoConstants.FileExtensions.WORK, GoConstants.FileExtensions.SUM -> GoModFileType
            else -> null
        }
    }

    override fun getDesiredContentPrefixLength(): Int = 0
}
