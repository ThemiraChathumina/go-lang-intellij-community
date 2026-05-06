package com.github.themirachathumina.golangintellijcommunity.gomod

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/*
 * Adds code folding for parenthesised blocks in go.mod and go.work files
 * (e.g. `require ( ... )`).
 */
class GoModFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val rootNode = root.node ?: return emptyArray()
        val descriptors = mutableListOf<FoldingDescriptor>()
        var lparen: ASTNode? = null
        var child = rootNode.firstChildNode
        while (child != null) {
            when (child.elementType) {
                GoModTokenTypes.LPAREN -> lparen = child
                GoModTokenTypes.RPAREN -> {
                    if (lparen != null) {
                        val start = lparen.startOffset
                        val end = child.startOffset + child.textLength
                        descriptors.add(FoldingDescriptor(rootNode, TextRange(start, end)))
                        lparen = null
                    }
                }
            }
            child = child.treeNext
        }
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "(...)"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
