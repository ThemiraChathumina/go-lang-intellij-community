package com.github.themirachathumina.golangintellijcommunity.ide

import com.github.themirachathumina.golangintellijcommunity.GoConstants
import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.packageSet.CustomScopesProvider
import com.intellij.psi.search.scope.packageSet.FilePatternPackageSet
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.psi.search.scope.packageSet.UnionPackageSet

class GoFileScopesProvider(private val project: Project) : CustomScopesProvider {

    companion object {
        const val TEST_FILES_SCOPE = "Go Test Files"
        const val SUM_FILE_SCOPE   = "Go Sum File"
    }

    override fun getCustomScopes(): List<NamedScope> = listOf(
        NamedScope(TEST_FILES_SCOPE, UnionPackageSet.create(
            FilePatternPackageSet(null, "*${GoConstants.FileExtensions.TEST_SUFFIX}"),
            FilePatternPackageSet(null, "**/*${GoConstants.FileExtensions.TEST_SUFFIX}")
        )),
        NamedScope(SUM_FILE_SCOPE, UnionPackageSet.create(
            FilePatternPackageSet(null, GoConstants.FileExtensions.SUM),
            FilePatternPackageSet(null, "**/${GoConstants.FileExtensions.SUM}")
        ))
    )
}
