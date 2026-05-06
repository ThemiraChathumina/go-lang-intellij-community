package com.github.themirachathumina.golangintellijcommunity

import com.intellij.lexer.FlexAdapter

/*
 * Wraps the JFlex-generated _GoLexer in IntelliJ's FlexAdapter so it can be used
 * as a standard Lexer by the parser and syntax highlighter.
 */
class GoLexerAdapter : FlexAdapter(_GoLexer(null))