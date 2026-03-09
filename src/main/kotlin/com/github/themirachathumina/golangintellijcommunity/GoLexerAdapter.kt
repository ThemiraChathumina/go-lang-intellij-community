package com.github.themirachathumina.golangintellijcommunity

import com.github.themirachathumina.golangintellijcommunity._GoLexer
import com.intellij.lexer.FlexAdapter

class GoLexerAdapter : FlexAdapter(_GoLexer(null))