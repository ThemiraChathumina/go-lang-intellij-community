# Go Language Plugin for IntelliJ Community Edition

## Project Overview

This is an IntelliJ Platform plugin that provides Go language support for IntelliJ IDEA Community Edition. It combines a custom parser/lexer implementation with LSP integration via `gopls`.

**Incompatibility Note:** The plugin is explicitly incompatible with IntelliJ IDEA Ultimate, which already has a native Go plugin.

## Architecture

The plugin is structured around two main pillars:

1. **Custom Parser & Editor Features** — Lexer-based syntax highlighting, annotators, and editor helpers implemented directly using the IntelliJ Platform APIs.
2. **LSP Bridge (gopls)** — Delegates advanced language intelligence (diagnostics, navigation, hints) to the Go Language Server via the `lsp4intellij` or similar LSP client bridge.

Key packages:
- `lsp/` — LSP client setup and gopls integration (`GoLanguageServer`, `GoLanguageServerFactory`)
- `sdk/` — SDK detection, project settings, status bar widget, and lifecycle management
- `run/` — Run configuration and profile state
- `wizard/` — New project wizard step

## Important Constraints

- **Do not read or modify** `src/main/gen/` — this folder contains auto-generated parser/lexer code from Grammar-Kit and should not be edited manually.
- The plugin targets **IntelliJ Community Edition only**. Do not introduce APIs or dependencies that require Ultimate.
- Keep LSP and custom parser concerns separate — avoid mixing gopls-provided features with custom annotator logic.

## Implemented Features

### Core Language & Editor Support
- Syntax highlighting via custom lexer
- Annotators: semantic highlighting, format verbs (`%v`, `%d`), escape sequences, regexp injection
- Code folding, brace matching, smart quotes, line/block commenting, spellcheck integration

### SDK & Project Management
- Auto-detection of installed Go versions
- SDK status bar widget
- New project wizard
- SDK misconfiguration notifications

### Run/Debug
- Run configurations and producers
- Line markers (gutter icons) for `main` functions and tests

### LSP Integration (gopls)
- Parameter info / signature hints
- Structure view
- Real-time error diagnostics
- Code navigation (go to definition, find usages)

## Development Workflow

**After every code change, run Kotlin compilation and fix any errors before considering the task done:**

```bash
./gradlew compileKotlin
```

Do not leave compilation errors unfixed. If errors are introduced by a change, resolve them immediately in the same task.

**After every successful change, provide a short one-line commit message** summarising what was done.

## Build & Development

This is a Gradle-based IntelliJ plugin project.

```bash
# Build the plugin
./gradlew build

# Run in a sandboxed IDE instance
./gradlew runIde

# Run tests
./gradlew test
```

Configuration files:
- `build.gradle.kts` — build configuration and plugin dependencies
- `gradle.properties` — plugin version, IntelliJ platform version
- `settings.gradle.kts` — project name and module setup
- `src/main/resources/META-INF/plugin.xml` — plugin descriptor (extensions, actions, incompatibilities)
