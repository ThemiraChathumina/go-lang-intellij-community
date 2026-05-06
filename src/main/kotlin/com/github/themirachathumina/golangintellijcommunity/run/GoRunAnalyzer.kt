package com.github.themirachathumina.golangintellijcommunity.run

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.github.themirachathumina.golangintellijcommunity.psi.GoPackageClause
import com.github.themirachathumina.golangintellijcommunity.psi.GoPackageName
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionName

/*
 * PSI-based utilities to determine whether a Go file belongs to the main package and declares func main().
 * Used by run configuration producers to decide if a run action should be offered.
 */
object GoRunAnalyzer {

    fun isMainPackage(file: PsiFile): Boolean {
        if (!file.name.endsWith(GoConstants.FileExtensions.GO)) return false

        // FIX: Use findChildOfType to search recursively past GoSourceFileImpl
        val packageClause = PsiTreeUtil.findChildOfType(file, GoPackageClause::class.java) ?: return false
        val packageName = PsiTreeUtil.findChildOfType(packageClause, GoPackageName::class.java)

        return packageName?.text == "main"
    }

    fun hasFuncMain(file: PsiFile): Boolean {
        if (!file.name.endsWith(GoConstants.FileExtensions.GO)) return false

        val functionDecls = PsiTreeUtil.findChildrenOfType(file, GoFunctionDecl::class.java)

        return functionDecls.any { function ->
            val functionName = PsiTreeUtil.findChildOfType(function, GoFunctionName::class.java)
            functionName?.text == "main"
        }
    }
}