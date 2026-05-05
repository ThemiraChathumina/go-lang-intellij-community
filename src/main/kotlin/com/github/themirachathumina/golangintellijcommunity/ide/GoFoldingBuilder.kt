package com.github.themirachathumina.golangintellijcommunity.ide

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.github.themirachathumina.golangintellijcommunity.psi.GoBlock
import com.github.themirachathumina.golangintellijcommunity.psi.GoImportDecl
import com.github.themirachathumina.golangintellijcommunity.psi.GoInterfaceType
import com.github.themirachathumina.golangintellijcommunity.psi.GoStructType
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GoFoldingBuilder : FoldingBuilderEx(), DumbAware {
    private val LOG = Logger.getInstance(GoFoldingBuilder::class.java)

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        LOG.debug("${GoConstants.Log.PREFIX} Building fold regions for ${root.containingFile?.name}")
        val descriptors = mutableListOf<FoldingDescriptor>()

        // 1. Blocks { ... }
        PsiTreeUtil.findChildrenOfType(root, GoBlock::class.java).forEach { block ->
            descriptors.add(FoldingDescriptor(block.node, block.textRange))
        }

        // 2. Imports import ( ... )
        PsiTreeUtil.findChildrenOfType(root, GoImportDecl::class.java).forEach { importDecl ->
            val lParen = importDecl.node.findChildByType(com.github.themirachathumina.golangintellijcommunity.psi.GoTypes.LPAREN)
            val rParen = importDecl.node.findChildByType(com.github.themirachathumina.golangintellijcommunity.psi.GoTypes.RPAREN)
            if (lParen != null && rParen != null) {
                descriptors.add(FoldingDescriptor(importDecl.node, TextRange(lParen.startOffset, rParen.startOffset + 1)))
            }
        }

        // 3. Structs struct { ... }
        PsiTreeUtil.findChildrenOfType(root, GoStructType::class.java).forEach { struct ->
            descriptors.add(FoldingDescriptor(struct.node, struct.textRange))
        }

        // 4. Interfaces interface { ... }
        PsiTreeUtil.findChildrenOfType(root, GoInterfaceType::class.java).forEach { iface ->
            descriptors.add(FoldingDescriptor(iface.node, iface.textRange))
        }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return when (node.psi) {
            is GoBlock -> "{...}"
            is GoImportDecl -> "(...)"
            is GoStructType -> "{...}"
            is GoInterfaceType -> "{...}"
            else -> "..."
        }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return node.psi is GoImportDecl
    }
}
