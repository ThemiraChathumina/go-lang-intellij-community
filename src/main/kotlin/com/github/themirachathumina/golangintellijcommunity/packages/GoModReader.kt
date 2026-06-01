package com.github.themirachathumina.golangintellijcommunity.packages

import com.google.gson.JsonStreamParser
import java.io.File

/*
 * Reads the current module's dependencies by running `go list -m -json [-u] all`.
 * Parses the multi-JSON-object output (one object per module, not a JSON array).
 * Pass checkUpdates=true to include the latest available version for each module;
 * this requires network access and is noticeably slower.
 */
object GoModReader {

    data class Result(val modules: List<GoModuleItem>, val mainModule: String)

    fun read(goExe: String, workDir: String, checkUpdates: Boolean): Result {
        val args = buildList {
            add(goExe); add("list"); add("-m"); add("-json")
            if (checkUpdates) add("-u")
            add("all")
        }

        val proc = ProcessBuilder(args)
            .directory(File(workDir))
            .redirectErrorStream(true)
            .start()
        val output = proc.inputStream.bufferedReader().readText()
        proc.waitFor()

        val modules = mutableListOf<GoModuleItem>()
        var mainModule = ""
        val parser = JsonStreamParser(output)

        while (true) {
            try {
                if (!parser.hasNext()) break
                val obj = parser.next().asJsonObject

                // The first entry (Main=true) is the project module itself — skip it
                if (obj.get("Main")?.asBoolean == true) {
                    mainModule = obj.get("Path")?.asString ?: ""
                    continue
                }

                val path = obj.get("Path")?.asString ?: continue
                val version = obj.get("Version")?.asString ?: continue
                val indirect = obj.get("Indirect")?.asBoolean ?: false
                val latestVersion = obj.getAsJsonObject("Update")?.get("Version")?.asString

                modules.add(GoModuleItem(path, version, latestVersion, indirect))
            } catch (_: Exception) {
                break
            }
        }

        return Result(modules, mainModule)
    }
}
