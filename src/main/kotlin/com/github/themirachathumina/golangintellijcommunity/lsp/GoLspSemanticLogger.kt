package com.github.themirachathumina.golangintellijcommunity.lsp

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import org.eclipse.lsp4j.jsonrpc.messages.Message
import java.util.concurrent.ConcurrentHashMap

/*
 * Debug helper that decodes and logs gopls semantic token responses. Intercepts outgoing textDocument
 * messages to maintain a live snapshot of each file's content, then resolves and logs each token
 * with its type and modifiers.
 */
class GoLspSemanticLogger {

    private val LOG = Logger.getInstance(GoLspSemanticLogger::class.java)
    private val gson = Gson()

    @Volatile private var tokenTypes: List<String> = emptyList()
    @Volatile private var tokenModifiers: List<String> = emptyList()

    // uri → file lines
    private val fileContents = ConcurrentHashMap<String, List<String>>()

    // request id → uri
    private val pendingRequests = ConcurrentHashMap<String, String>()

    fun onMessage(message: Message) {
        val obj = try {
            JsonParser.parseString(gson.toJson(message)).asJsonObject
        } catch (_: Exception) { return }

        val method = obj.get("method")?.asString

        if (method != null) {
            handleOutgoing(method, obj)
        } else {
            handleIncoming(obj)
        }
    }

    // -------------------------------------------------------------------------
    // Client→server: track file contents and pending semantic token requests
    // -------------------------------------------------------------------------

    private fun handleOutgoing(method: String, obj: JsonObject) {
        when (method) {
            "textDocument/didOpen" -> {
                val td   = obj.getAsJsonObject("params")?.getAsJsonObject("textDocument") ?: return
                val uri  = td.get("uri")?.asString  ?: return
                val text = td.get("text")?.asString ?: return
                fileContents[uri] = splitLines(text)
            }

            "textDocument/didChange" -> {
                val params  = obj.getAsJsonObject("params") ?: return
                val uri     = params.getAsJsonObject("textDocument")?.get("uri")?.asString ?: return
                val changes = params.getAsJsonArray("contentChanges") ?: return
                for (change in changes) {
                    val c = change.asJsonObject
                    if (!c.has("range")) {
                        fileContents[uri] = splitLines(c.get("text")?.asString ?: continue)
                    } else {
                        val range   = c.getAsJsonObject("range") ?: continue
                        val newText = c.get("text")?.asString   ?: continue
                        applyRangeEdit(uri, range, newText)
                    }
                }
            }

            "textDocument/semanticTokens/full" -> {
                val uri = obj.getAsJsonObject("params")
                    ?.getAsJsonObject("textDocument")
                    ?.get("uri")?.asString ?: return
                val id = extractId(obj) ?: return
                pendingRequests[id] = uri
            }
        }
    }

    // -------------------------------------------------------------------------
    // Server→client responses (no "method" field)
    // -------------------------------------------------------------------------

    private fun handleIncoming(obj: JsonObject) {
        val resultElement = obj.get("result") ?: return
        if (!resultElement.isJsonObject) return
        val result = resultElement.asJsonObject
        val id     = extractId(obj)

        // initialize response → capture token legend
        if (result.has("capabilities")) {
            val legend = result.getAsJsonObject("capabilities")
                ?.getAsJsonObject("semanticTokensProvider")
                ?.getAsJsonObject("legend") ?: return
            tokenTypes     = legend.getAsJsonArray("tokenTypes")?.map  { it.asString } ?: emptyList()
            tokenModifiers = legend.getAsJsonArray("tokenModifiers")?.map { it.asString } ?: emptyList()
            LOG.debug("[SemanticTokens] Legend loaded: ${tokenTypes.size} types, ${tokenModifiers.size} modifiers")
            return
        }

        // semantic tokens response
        if (id == null) return
        val uri       = pendingRequests.remove(id) ?: return
        val dataArray = result.getAsJsonArray("data") ?: return
        if (dataArray.size() == 0) return

        val data     = dataArray.map { it.asInt }
        val lines    = fileContents[uri]
        val fileName = uri.substringAfterLast("/")

        LOG.debug("\n[SemanticTokens] $fileName")
        decode(data, lines)
    }

    // -------------------------------------------------------------------------
    // Content helpers
    // -------------------------------------------------------------------------

    /** Split on any line ending and strip trailing CR so stored lines are clean. */
    private fun splitLines(text: String): List<String> =
        text.split("\n").map { it.trimEnd('\r') }

    /** Apply a single LSP range edit to the stored file lines. */
    private fun applyRangeEdit(uri: String, range: JsonObject, newText: String) {
        val lines     = fileContents[uri]?.toMutableList() ?: return
        val startLine = range.getAsJsonObject("start")?.get("line")?.asInt ?: return
        val startChar = range.getAsJsonObject("start")?.get("character")?.asInt ?: return
        val endLine   = range.getAsJsonObject("end")?.get("line")?.asInt ?: return
        val endChar   = range.getAsJsonObject("end")?.get("character")?.asInt ?: return

        val prefix      = lines.getOrNull(startLine)?.take(startChar) ?: return
        val suffix      = lines.getOrNull(endLine)?.drop(endChar)     ?: return
        val replacement = splitLines(prefix + newText + suffix)

        repeat(endLine - startLine + 1) { lines.removeAt(startLine) }
        lines.addAll(startLine, replacement)
        fileContents[uri] = lines
    }

    // -------------------------------------------------------------------------
    // Decode delta-encoded token array
    // -------------------------------------------------------------------------

    private fun decode(data: List<Int>, lines: List<String>?) {
        var line = 0
        var char = 0
        var i    = 0
        while (i + 5 <= data.size) {
            val deltaLine  = data[i]
            val deltaStart = data[i + 1]
            val length     = data[i + 2]
            val typeIdx    = data[i + 3]
            val modBits    = data[i + 4]
            i += 5

            if (deltaLine > 0) { line += deltaLine; char = deltaStart }
            else               { char += deltaStart }

            val typeName = tokenTypes.getOrElse(typeIdx) { "unknown($typeIdx)" }
            val mods     = tokenModifiers.filterIndexed { bit, _ -> modBits and (1 shl bit) != 0 }

            val text = lines?.getOrNull(line)?.let { row ->
                if (char + length <= row.length) row.substring(char, char + length) else "?"
            } ?: "${line}:${char}"

            val modStr = if (mods.isEmpty()) "" else " [${mods.joinToString(", ")}]"
            LOG.debug("  $text → $typeName$modStr")
        }
    }

    private fun extractId(obj: JsonObject): String? {
        val id = obj.getAsJsonObject("id") ?: return null
        return id.get("left")?.asString ?: id.get("right")?.asString
    }
}
