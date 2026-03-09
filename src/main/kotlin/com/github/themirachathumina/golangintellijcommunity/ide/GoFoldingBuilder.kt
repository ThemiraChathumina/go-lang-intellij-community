package com.github.themirachathumina.golangintellijcommunity.ide

import com.github.themirachathumina.golangintellijcommunity.psi.*
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GoFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        PsiTreeUtil.processElements(root) { element ->
            when (element) {
                is GoBlock -> {
                    addDescriptor(descriptors, element, "{", "}")
                }
                is GoStructType -> {
                    addDescriptor(descriptors, element, "{", "}")
                }
                is GoInterfaceType -> {
                    addDescriptor(descriptors, element, "{", "}")
                }
                is GoImportDecl -> {
                    val lparen = element.node.findChildByType(GoTypes.LPAREN)
                    val rparen = element.node.findChildByType(GoTypes.RPAREN)
                    if (lparen != null && rparen != null) {
                        descriptors.add(FoldingDescriptor(element.node, TextRange(lparen.startOffset, rparen.startOffset + 1)))
                    }
                }
                is GoConstDecl -> {
                    addGroupedDescriptor(descriptors, element)
                }
                is GoVarDecl -> {
                    addGroupedDescriptor(descriptors, element)
                }
                is GoTypeDecl -> {
                    addGroupedDescriptor(descriptors, element)
                }
            }
            true
        }

        return descriptors.toTypedArray()
    }

    private fun addDescriptor(descriptors: MutableList<FoldingDescriptor>, element: PsiElement, startToken: String, endToken: String) {
        val range = element.textRange
        if (range.length > 2) { // Only fold if not empty {}
            descriptors.add(FoldingDescriptor(element.node, range))
        }
    }

    private fun addGroupedDescriptor(descriptors: MutableList<FoldingDescriptor>, element: PsiElement) {
        val lparen = element.node.findChildByType(GoTypes.LPAREN)
        val rparen = element.node.findChildByType(GoTypes.RPAREN)
        if (lparen != null && rparen != null) {
            descriptors.add(FoldingDescriptor(element.node, TextRange(lparen.startOffset, rparen.startOffset + 1)))
        }
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        val type = node.elementType
        if (type == GoTypes.BLOCK || type == GoTypes.STRUCT_TYPE || type == GoTypes.INTERFACE_TYPE) {
            return "{...}"
        }
        if (type == GoTypes.IMPORT_DECL || type == GoTypes.CONST_DECL || type == GoTypes.VAR_DECL || type == GoTypes.TYPE_DECL) {
            return "(...)"
        }
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
