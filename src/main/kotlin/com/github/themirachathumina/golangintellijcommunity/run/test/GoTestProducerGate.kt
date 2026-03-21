package com.github.themirachathumina.golangintellijcommunity.run.test

import com.github.themirachathumina.golangintellijcommunity.GoFile
import com.github.themirachathumina.golangintellijcommunity.psi.GoFunctionDecl
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

object GoTestProducerGate {

    private val bypass = ThreadLocal.withInitial { false }

    fun isBypassed(): Boolean = bypass.get()

    fun <T> withBypass(block: () -> T): T {
        val prev = bypass.get()
        bypass.set(true)
        return try { block() } finally { bypass.set(prev) }
    }

    /**
     * Returns true when producers should suppress themselves for the platform's
     * native context-menu call.
     *
     * Suppression rules:
     * - Inside a **fuzz function**: always suppress. Our context groups handle fuzz
     *   by showing a "Run" submenu with both a unit-test variant and a -fuzz variant.
     * - Inside any **other function** (Test, Benchmark, Example, table-subtest):
     *   do NOT suppress. The single relevant producer creates the exact function-scope
     *   config and the platform shows it as a flat action.
     * - At **file/package level** (cursor not inside any function): suppress when
     *   the file contains 2 or more distinct function kinds (non-fuzz tests,
     *   fuzz tests, benchmarks), so the multiple matching producers are consolidated
     *   into our "Run" submenu rather than shown as separate platform actions.
     */
    fun shouldSuppress(file: GoFile, element: PsiElement): Boolean {
        if (isBypassed()) return false

        val function = PsiTreeUtil.getParentOfType(element, GoFunctionDecl::class.java)

        // Inside a fuzz function — suppress so our context groups own the submenu
        if (function != null && GoFuzzRunConfigurationProducer.isFuzzFunction(function)) return true

        // Inside any other function — don't suppress, let the single matching producer fire
        if (function != null) return false

        // At file level — suppress when 2+ distinct function kinds are present so our
        // submenus consolidate them.  Fuzz is counted separately from non-fuzz tests
        // because GoFuzzRunConfigurationProducer now creates its own file-scope config.
        val functions = PsiTreeUtil.findChildrenOfType(file, GoFunctionDecl::class.java)
        val hasNonFuzzTests = functions.any {
            GoTestRunConfigurationProducer.isTestFunction(it) && !GoFuzzRunConfigurationProducer.isFuzzFunction(it)
        }
        val hasFuzz       = functions.any { GoFuzzRunConfigurationProducer.isFuzzFunction(it) }
        val hasBenchmarks = functions.any { GoBenchmarkRunConfigurationProducer.isBenchmarkFunction(it) }
        return listOf(hasNonFuzzTests, hasFuzz, hasBenchmarks).count { it } >= 2
    }
}
