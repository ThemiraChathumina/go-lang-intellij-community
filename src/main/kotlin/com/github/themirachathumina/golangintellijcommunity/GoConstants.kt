package com.github.themirachathumina.golangintellijcommunity

/*
 * Central repository of compile-time string constants used throughout the plugin:
 * Go environment variable names, executable names, file extensions, log prefix,
 * minimum SDK version, and LSP server ID.
 */
object GoConstants {

    object Env {
        const val GOROOT = "GOROOT"
        const val GOPATH = "GOPATH"
        const val GOMODCACHE = "GOMODCACHE"
        const val GOPROXY = "GOPROXY"
        const val GOARCH = "GOARCH"
        const val GOOS = "GOOS"
        const val CGO_ENABLED = "CGO_ENABLED"
        const val GOTOOLCHAIN = "GOTOOLCHAIN"
        const val GOCOVERDIR = "GOCOVERDIR"
    }

    object Executables {
        const val GO = "go"
        const val GOPLS = "gopls"
        const val GOTESTS = "gotests"
    }

    object FileExtensions {
        /** With leading dot — for String.endsWith() and File.name checks. */
        const val GO = ".go"
        /** Without leading dot — for VirtualFile.extension comparisons. */
        const val GO_EXT = "go"
        const val TEST_SUFFIX = "_test.go"
        const val MOD = "go.mod"
        const val WORK = "go.work"
        const val SUM = "go.sum"
        const val GO_VERSION = ".go-version"
    }

    object Log {
        const val PREFIX = "[Go-Lang-Community]"
    }

    object Sdk {
        const val MIN_VERSION = "1.21"
        const val VERSION_FILE = "VERSION"
        const val VERSION_PREFIX = "go"
    }

    object Lsp {
        /** lsp4ij server ID as declared in plugin.xml. */
        const val SERVER_ID = "gopls"
    }
}
