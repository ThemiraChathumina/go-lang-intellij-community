package com.github.themirachathumina.golangintellijcommunity.run.test

/*
 * Enum representing the different scopes for a Go test run: 
 * an entire directory, a single package, a specific file, or an individual test function.
 */
enum class GoTestScope {
    DIRECTORY, PACKAGE, FILE, FUNCTION
}
