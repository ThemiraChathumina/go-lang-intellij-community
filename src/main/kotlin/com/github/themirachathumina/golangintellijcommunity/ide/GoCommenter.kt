package com.github.themirachathumina.golangintellijcommunity.ide

import com.intellij.lang.Commenter

/*
 * Commenter for Go files. Provides // for line comments and /* */ for block comments,
 * enabling the Ctrl+/ and Ctrl+Shift+/ IDE shortcuts.
 */
class GoCommenter : Commenter {
    override fun getLineCommentPrefix(): String = "//"

    override fun getBlockCommentPrefix(): String = "/*"

    override fun getBlockCommentSuffix(): String = "*/"

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}
