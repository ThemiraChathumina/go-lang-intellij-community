package com.github.themirachathumina.golangintellijcommunity.highlight

import com.github.themirachathumina.golangintellijcommunity.GoIcons
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class GoColorSettingsPage : ColorSettingsPage {

    companion object {
        private val DESCRIPTORS = arrayOf(
            // ── Lexer (always active) ────────────────────────────────────────────
            AttributesDescriptor("Keyword",             GoSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Number",              GoSyntaxHighlighter.NUMBER),
            AttributesDescriptor("String",              GoSyntaxHighlighter.STRING),
            AttributesDescriptor("Line Comment",        GoSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Block Comment",       GoSyntaxHighlighter.BLOCK_COMMENT),
            AttributesDescriptor("Operator",            GoSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Braces",              GoSyntaxHighlighter.BRACES),
            AttributesDescriptor("Brackets",            GoSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Parentheses",         GoSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Bad Character",       GoSyntaxHighlighter.BAD_CHARACTER),
            AttributesDescriptor("Valid String Escape", GoSyntaxHighlighter.VALID_STRING_ESCAPE),

            // ── PSI annotator (always active, no LSP required) ───────────────────
            AttributesDescriptor("Structs//Tag",        GoSyntaxHighlighter.STRUCT_TAG),

            // ── LSP Semantic Tokens (require gopls + semantic tokens enabled) ────
            AttributesDescriptor("LSP Semantic Tokens//Package Name",                            GoSyntaxHighlighter.PACKAGE_NAME),
            AttributesDescriptor("LSP Semantic Tokens//Types//Type Name",                        GoSyntaxHighlighter.TYPE_NAME),
            AttributesDescriptor("LSP Semantic Tokens//Types//Built-in Type",                    GoSyntaxHighlighter.BUILTIN_TYPE),
            AttributesDescriptor("LSP Semantic Tokens//Types//Interface Type",                   GoSyntaxHighlighter.INTERFACE_TYPE),
            AttributesDescriptor("LSP Semantic Tokens//Types//Struct Type",                      GoSyntaxHighlighter.STRUCT_TYPE),
            AttributesDescriptor("LSP Semantic Tokens//Types//Type Parameter",                   GoSyntaxHighlighter.TYPE_PARAMETER),
            AttributesDescriptor("LSP Semantic Tokens//Functions//Function Declaration",         GoSyntaxHighlighter.FUNCTION_DECLARATION),
            AttributesDescriptor("LSP Semantic Tokens//Functions//Method Declaration",           GoSyntaxHighlighter.METHOD_DECLARATION),
            AttributesDescriptor("LSP Semantic Tokens//Functions//Function Call",                GoSyntaxHighlighter.FUNCTION_CALL),
            AttributesDescriptor("LSP Semantic Tokens//Functions//Method Call",                  GoSyntaxHighlighter.METHOD_CALL),
            AttributesDescriptor("LSP Semantic Tokens//Variables//Constant",                     GoSyntaxHighlighter.CONSTANT),
            AttributesDescriptor("LSP Semantic Tokens//Variables//Predeclared (nil/true/false)", GoSyntaxHighlighter.PREDECLARED),
            AttributesDescriptor("LSP Semantic Tokens//Variables//Variable (function type)",     GoSyntaxHighlighter.VAR_FUNC),
            AttributesDescriptor("LSP Semantic Tokens//Variables//Variable (interface type)",    GoSyntaxHighlighter.VAR_INTERFACE),
            AttributesDescriptor("LSP Semantic Tokens//Variables//Variable (pointer type)",      GoSyntaxHighlighter.VAR_POINTER),
            AttributesDescriptor("LSP Semantic Tokens//Variables//Variable (string type)",       GoSyntaxHighlighter.VAR_STRING),
            AttributesDescriptor("LSP Semantic Tokens//Variables//Variable (number type)",       GoSyntaxHighlighter.VAR_NUMBER),
            AttributesDescriptor("LSP Semantic Tokens//Parameters//Parameter",                   GoSyntaxHighlighter.PARAMETER),
            AttributesDescriptor("LSP Semantic Tokens//Parameters//Parameter (interface type)",  GoSyntaxHighlighter.PARAM_INTERFACE),
            AttributesDescriptor("LSP Semantic Tokens//Parameters//Parameter (struct type)",     GoSyntaxHighlighter.PARAM_STRUCT),
            AttributesDescriptor("LSP Semantic Tokens//Struct Field",                            GoSyntaxHighlighter.STRUCT_FIELD),
            AttributesDescriptor("LSP Semantic Tokens//Format Verb (%s, %d, ...)",               GoSyntaxHighlighter.FORMAT_VERB)
        )
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Go"

    override fun getIcon(): Icon = GoIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = GoSyntaxHighlighter()

    override fun getDemoText(): String = """
        package main

        import (
            "fmt"
            "context"
        )

        // This is a line comment
        /* This is a block comment */

        const MaxRetries = 3

        type <structType>Server</structType> struct {
            <field>Host</field> <builtin>string</builtin> <tag>`json:"host"`</tag>
            <field>Port</field> <builtin>int</builtin>    <tag>`json:"port"`</tag>
        }

        type <interfaceType>Handler</interfaceType> interface {
            <methodDecl>Handle</methodDecl>(ctx <pkg>context</pkg>.<interfaceType>Context</interfaceType>) <builtin>error</builtin>
        }

        func <funcDecl>NewServer</funcDecl>[<typeParam>T</typeParam> <interfaceType>Handler</interfaceType>](
            <param>host</param> <builtin>string</builtin>,
            <param>port</param> <builtin>int</builtin>,
            <paramIface>h</paramIface> <interfaceType>Handler</interfaceType>,
            <paramStruct>s</paramStruct> <structType>Server</structType>,
        ) *<structType>Server</structType> {
            <pkg>fmt</pkg>.<funcCall>Println</funcCall>("retries:", MaxRetries)
            var fn <varFunc>= func() {}</varFunc>
            var iface <varIface>Handler</varIface>
            var ptr <varPtr>*Server</varPtr>
            var name <varStr>"value"</varStr>
            var count <varNum>42</varNum>
            _ = fn; _ = iface; _ = ptr; _ = name; _ = count
            enabled := <predecl>true</predecl>
            result := <predecl>nil</predecl>
            _ = enabled; _ = result
            return &<structType>Server</structType>{<field>Host</field>: <param>host</param>, <field>Port</field>: <param>port</param>}
        }

        func (s *<structType>Server</structType>) <methodDecl>Start</methodDecl>(<param>ctx</param> <pkg>context</pkg>.<interfaceType>Context</interfaceType>) <builtin>error</builtin> {
            msg := "listen<escape>\t</escape><escape>\n</escape>"
            <pkg>fmt</pkg>.<funcCall>Sprintf</funcCall>("<fmtVerb>%s</fmtVerb> <fmtVerb>%d</fmtVerb>", s.<field>Host</field>, s.<field>Port</field>)
            _ = msg
            return <predecl>nil</predecl>
        }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey> {
        return mutableMapOf(
            "builtin"       to GoSyntaxHighlighter.BUILTIN_TYPE,
            "funcDecl"      to GoSyntaxHighlighter.FUNCTION_DECLARATION,
            "methodDecl"    to GoSyntaxHighlighter.METHOD_DECLARATION,
            "funcCall"      to GoSyntaxHighlighter.FUNCTION_CALL,
            "methodCall"    to GoSyntaxHighlighter.METHOD_CALL,
            "field"         to GoSyntaxHighlighter.STRUCT_FIELD,
            "tag"           to GoSyntaxHighlighter.STRUCT_TAG,
            "escape"        to GoSyntaxHighlighter.VALID_STRING_ESCAPE,
            "pkg"           to GoSyntaxHighlighter.PACKAGE_NAME,
            "interfaceType" to GoSyntaxHighlighter.INTERFACE_TYPE,
            "structType"    to GoSyntaxHighlighter.STRUCT_TYPE,
            "param"         to GoSyntaxHighlighter.PARAMETER,
            "paramIface"    to GoSyntaxHighlighter.PARAM_INTERFACE,
            "paramStruct"   to GoSyntaxHighlighter.PARAM_STRUCT,
            "typeParam"     to GoSyntaxHighlighter.TYPE_PARAMETER,
            "predecl"       to GoSyntaxHighlighter.PREDECLARED,
            "varFunc"       to GoSyntaxHighlighter.VAR_FUNC,
            "varIface"      to GoSyntaxHighlighter.VAR_INTERFACE,
            "varPtr"        to GoSyntaxHighlighter.VAR_POINTER,
            "varStr"        to GoSyntaxHighlighter.VAR_STRING,
            "varNum"        to GoSyntaxHighlighter.VAR_NUMBER,
            "fmtVerb"       to GoSyntaxHighlighter.FORMAT_VERB
        )
    }
}
