package com.github.themirachathumina.golangintellijcommunity.wizard

import java.io.File

object GoProjectScaffolder {

    private val NON_IDENTIFIER_REGEX = Regex("[^a-z0-9_]")

    fun scaffold(projectType: GoProjectType, root: File, projectName: String, modulePath: String) {
        when (projectType) {
            GoProjectType.EXECUTABLE -> scaffoldExecutable(root)
            GoProjectType.LIBRARY    -> scaffoldLibrary(root, projectName)
            GoProjectType.CLI        -> scaffoldCli(root, projectName, modulePath)
        }
    }

    private fun scaffoldExecutable(root: File) {
        File(root, "main.go").takeIf { !it.exists() }?.writeText(
            """
            package main

            import "fmt"

            func main() {
                fmt.Println("Hello, World!")
            }
            """.trimIndent() + "\n"
        )
    }

    private fun scaffoldLibrary(root: File, projectName: String) {
        val pkgName = toGoIdentifier(projectName)
        File(root, "$pkgName.go").takeIf { !it.exists() }?.writeText(
            """
            package $pkgName

            // Hello returns a greeting for the given name.
            func Hello(name string) string {
                return "Hello, " + name + "!"
            }
            """.trimIndent() + "\n"
        )
    }

    private fun scaffoldCli(root: File, projectName: String, modulePath: String) {
        val pkgName = toGoIdentifier(projectName)

        val cmdDir = File(root, "cmd/$pkgName").also { it.mkdirs() }
        File(cmdDir, "main.go").takeIf { !it.exists() }?.writeText(
            """
            package main

            import (
                "fmt"
                "$modulePath/internal/$pkgName"
            )

            func main() {
                fmt.Println($pkgName.Greeting())
            }
            """.trimIndent() + "\n"
        )

        val internalDir = File(root, "internal/$pkgName").also { it.mkdirs() }
        File(internalDir, "$pkgName.go").takeIf { !it.exists() }?.writeText(
            """
            package $pkgName

            // Greeting returns the application greeting.
            func Greeting() string {
                return "Hello from $projectName!"
            }
            """.trimIndent() + "\n"
        )
    }

    fun toGoIdentifier(name: String): String =
        name.lowercase().replace(NON_IDENTIFIER_REGEX, "_").trimStart('_').ifBlank { "pkg" }
}
