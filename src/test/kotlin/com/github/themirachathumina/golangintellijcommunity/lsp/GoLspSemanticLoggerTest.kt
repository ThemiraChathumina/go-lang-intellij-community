package com.github.themirachathumina.golangintellijcommunity.lsp

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.eclipse.lsp4j.jsonrpc.messages.Message
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Field

class GoLspSemanticLoggerTest {

    // A dummy Message class to satisfy the interface for testing
    class DummyMessage(val method: String?, val params: JsonObject? = null, val result: JsonObject? = null, val id: JsonObject? = null) : Message() {
        override fun getJsonrpc(): String = "2.0"
    }

    @Test
    fun testHandleOutgoing_didOpen() {
        val logger = GoLspSemanticLogger()
        
        val textDocument = JsonObject()
        textDocument.addProperty("uri", "file:///test.go")
        textDocument.addProperty("text", "package main\n\nfunc main() {}")
        val params = JsonObject()
        params.add("textDocument", textDocument)
        
        val msg = DummyMessage(method = "textDocument/didOpen", params = params)
        
        logger.onMessage(msg)
        
        // Use reflection to verify internal state since it's private
        val field: Field = GoLspSemanticLogger::class.java.getDeclaredField("fileContents")
        field.isAccessible = true
        val fileContents = field.get(logger) as Map<*, *>
        
        val lines = fileContents["file:///test.go"] as List<*>
        assertEquals(3, lines.size)
        assertEquals("package main", lines[0])
        assertEquals("func main() {}", lines[2])
    }

    @Test
    fun testHandleIncoming_Legend() {
        val logger = GoLspSemanticLogger()
        
        val legend = JsonObject()
        val tokenTypes = com.google.gson.JsonArray().apply { add("namespace"); add("type") }
        val tokenModifiers = com.google.gson.JsonArray().apply { add("declaration"); add("readonly") }
        legend.add("tokenTypes", tokenTypes)
        legend.add("tokenModifiers", tokenModifiers)
        
        val provider = JsonObject()
        provider.add("legend", legend)
        val capabilities = JsonObject()
        capabilities.add("semanticTokensProvider", provider)
        
        val result = JsonObject()
        result.add("capabilities", capabilities)
        
        val msg = DummyMessage(method = null, result = result, id = JsonObject().apply { addProperty("left", "1") })
        
        logger.onMessage(msg)
        
        // Use reflection to verify internal state
        val typesField: Field = GoLspSemanticLogger::class.java.getDeclaredField("tokenTypes")
        typesField.isAccessible = true
        val types = typesField.get(logger) as List<*>
        assertEquals("namespace", types[0])
        
        val modField: Field = GoLspSemanticLogger::class.java.getDeclaredField("tokenModifiers")
        modField.isAccessible = true
        val mods = modField.get(logger) as List<*>
        assertEquals("readonly", mods[1])
    }
}
