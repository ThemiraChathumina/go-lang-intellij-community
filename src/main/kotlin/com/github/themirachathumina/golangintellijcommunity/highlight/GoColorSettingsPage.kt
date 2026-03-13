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
            AttributesDescriptor("Keyword", GoSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Number", GoSyntaxHighlighter.NUMBER),
            AttributesDescriptor("String", GoSyntaxHighlighter.STRING),
            AttributesDescriptor("Line Comment", GoSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Block Comment", GoSyntaxHighlighter.BLOCK_COMMENT),
            AttributesDescriptor("Operator", GoSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Braces", GoSyntaxHighlighter.BRACES),
            AttributesDescriptor("Brackets", GoSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Parentheses", GoSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Bad Character", GoSyntaxHighlighter.BAD_CHARACTER),

            AttributesDescriptor("Valid String Escape", GoSyntaxHighlighter.VALID_STRING_ESCAPE),

            // Semantic Descriptors
            AttributesDescriptor("Types//Type Name", GoSyntaxHighlighter.TYPE_NAME),
            AttributesDescriptor("Types//Built-in Type", GoSyntaxHighlighter.BUILTIN_TYPE),
            AttributesDescriptor("Functions//Function Declaration", GoSyntaxHighlighter.FUNCTION_DECLARATION),
            AttributesDescriptor("Functions//Method Declaration", GoSyntaxHighlighter.METHOD_DECLARATION),
            AttributesDescriptor("Functions//Function Call", GoSyntaxHighlighter.FUNCTION_CALL),
            AttributesDescriptor("Functions//Method Call", GoSyntaxHighlighter.METHOD_CALL),
            AttributesDescriptor("Structs//Field", GoSyntaxHighlighter.STRUCT_FIELD),
            AttributesDescriptor("Structs//Tag", GoSyntaxHighlighter.STRUCT_TAG),
            AttributesDescriptor("Constant", GoSyntaxHighlighter.CONSTANT)
        )
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Go"

    override fun getIcon(): Icon = GoIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = GoSyntaxHighlighter()

    override fun getDemoText(): String = """
        package main
        
        import "fmt"
        
        // This is a line comment
        /*
           This is a block comment
        */
        type <typeName>User</typeName> struct {
            <field>Name</field> <builtin>string</builtin> <tag>`json:"name"`</tag>
            <field>Age</field>  <builtin>int</builtin>    <tag>`json:"age"`</tag>
        }
        
        func (u *<typeName>User</typeName>) <methodDecl>Print</methodDecl>() {
            fmt.<methodCall>Println</methodCall>(u.<field>Name</field>)
        }
        
        func <funcDecl>main</funcDecl>() {
            var count <builtin>int</builtin> = 42
         
            var message <builtin>string</builtin> = "Line 1<escape>\n</escape>Line 2<escape>\t</escape>Space"
            var runeVal <builtin>rune</builtin> = '<escape>\u00E4</escape>'
            var raw <builtin>string</builtin> = `Line 1\nLine 2`
            
            u := <funcCall>make</funcCall>(<typeName>User</typeName>)
            u.<methodCall>Print</methodCall>()
            
            if count > 0 {
                count++
            }
        }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey> {
        return mutableMapOf(
            "typeName" to GoSyntaxHighlighter.TYPE_NAME,
            "builtin" to GoSyntaxHighlighter.BUILTIN_TYPE,
            "funcDecl" to GoSyntaxHighlighter.FUNCTION_DECLARATION,
            "methodDecl" to GoSyntaxHighlighter.METHOD_DECLARATION,
            "funcCall" to GoSyntaxHighlighter.FUNCTION_CALL,
            "methodCall" to GoSyntaxHighlighter.METHOD_CALL,
            "field" to GoSyntaxHighlighter.STRUCT_FIELD,
            "tag" to GoSyntaxHighlighter.STRUCT_TAG,
            "escape" to GoSyntaxHighlighter.VALID_STRING_ESCAPE
        )
    }
}