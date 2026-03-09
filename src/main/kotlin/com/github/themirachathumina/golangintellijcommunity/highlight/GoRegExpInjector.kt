package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.psi.GoDeclaration
import com.github.themirachathumina.golangintellijcommunity.psi.GoSimpleStmt
import com.github.themirachathumina.golangintellijcommunity.psi.GoStringLiteral
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.lang.regexp.RegExpLanguage

class GoRegExpInjector : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context !is GoStringLiteral) return

        val text = context.text
        if (text.length < 2) return

        val statement = PsiTreeUtil.getParentOfType(
            context,
            GoSimpleStmt::class.java,
            GoDeclaration::class.java
        )

        val stmtText = statement?.text ?: ""

        // 1. Check if it's inside ANY standard regexp function call
        // (expanded to include Match, MatchString, etc.)
        val isRegExpCall = stmtText.contains("regexp.Compile") ||
                stmtText.contains("regexp.MustCompile") ||
                stmtText.contains("regexp.Match")

        // 2. Check if it's assigned to a common regex variable name
        val isRegexVarAssignment = stmtText.matches(Regex(".*\\b(pattern|regex|regexp|re)\\s*(:=|=).*"))

        // 3. NEW: Smart Heuristic for Raw Strings
        // If it's a Go raw string (`...`) and contains obvious regex metacharacters
        // like \d, \w, (?:, or character ranges like [a-z], inject it automatically!
        val isRawString = text.startsWith("`") && text.endsWith("`")
        val regexHeuristic = Regex("(\\\\[dDwWsS]|\\(\\?:|\\[[a-zA-Z]-|\\^.*\\$)")
        val looksLikeRegex = isRawString && regexHeuristic.containsMatchIn(text)

        // If ANY of these conditions are true, inject the RegExp language!
        if (isRegExpCall || isRegexVarAssignment || looksLikeRegex) {
            registrar.startInjecting(RegExpLanguage.INSTANCE)

            // Strip the surrounding quotes/backticks so they aren't parsed as part of the regex
            val contentRange = TextRange(1, text.length - 1)
            registrar.addPlace(null, null, context, contentRange)

            registrar.doneInjecting()
        }
    }

    // Register which PSI elements this injector should inspect
    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return listOf(GoStringLiteral::class.java)
    }
}