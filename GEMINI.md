# Go Language Plugin for IntelliJ Community Edition

## Project Overview

This is an IntelliJ Platform plugin that provides Go language support for IntelliJ IDEA Community Edition. It combines a custom parser/lexer implementation with LSP integration via `gopls`.

**Incompatibility Note:** The plugin is explicitly incompatible with IntelliJ IDEA Ultimate, which already has a native Go plugin.

## Architecture

The plugin is structured around two main pillars:

1. **Custom Parser & Editor Features** — Lexer-based syntax highlighting, annotators, and editor helpers implemented directly using the IntelliJ Platform APIs.
2. **LSP Bridge (gopls)** — Delegates advanced language intelligence (diagnostics, navigation, hints) to the Go Language Server via the `lsp4ij` LSP client bridge.

Key packages:
- `lsp/` — LSP client setup and gopls integration (`GoLanguageServer`, `GoLanguageServerFactory`)
- `sdk/` — SDK detection, project settings, status bar widget, and lifecycle management
- `run/` — Run configuration and profile state
- `wizard/` — New project wizard step
- `ide/` — Editor features (folding, brace matching, commenting, annotators, etc.)
- `gomod/` — go.mod / go.work / go.sum file type support
- `file/` — File creation templates and actions
- `settings/` — Application and project settings

## Important Constraints

- **Do not read or modify** `src/main/gen/` — this folder contains auto-generated parser/lexer code from Grammar-Kit and should not be edited manually.
- The plugin targets **IntelliJ Community Edition only**. Do not introduce APIs or dependencies that require Ultimate.
- Keep LSP and custom parser concerns separate — avoid mixing gopls-provided features with custom annotator logic.

## Implemented Features

### Core Language & File Types
- Go language definition and `.go` file type registration with icon
- GoMod language for `go.mod`, `go.work`, `go.sum` files with custom icons
- PSI file base class, lexer/parser adapter, and element factory (auto-generated from Grammar-Kit)

### Syntax Highlighting
- Lexer-based syntax highlighter mapping 50+ token types to color attributes
- Color settings page with customizable preview and demo code
- Theme-specific color schemes: Default, Darcula, High Contrast, Classic Light
- go.mod syntax highlighter (keywords, module paths, versions, comments)

### Semantic Highlighting (LSP)
- gopls semantic tokens mapped to IDE `TextAttributesKey`
- Distinct colors for: namespace, variable, function, type, property, parameter, typeParameter, string
- Modifiers: readonly, defaultLibrary, interface, pointer, struct, string, number, definition, signature
- Special highlighting for predeclared identifiers (`nil`, `true`, `false`)
- PSI annotator for struct tag keys in backtick strings

### Annotators
- **Format verb annotator** — highlights `%s`, `%d`, `%+v`, `%.2f`, `%05d`, `%%`, etc. in string literals
- **Escape sequence annotator** — highlights valid Go escape sequences (`\n`, `\t`, `\uXXXX`, `\xhh`, etc.)
- **Error highlighting filter** — suppresses `PsiErrorElement` noise in Go files

### Language Injection
- Injects RegExp language into string literals used with `regexp.Compile`/`MustCompile`/`Match`
- Injects RegExp into assignments to common regex variable names (`pattern`, `regex`, `re`, etc.)
- Heuristic injection into raw strings containing regex metacharacters

### Editor Features
- **Code folding** — blocks `{}`, import groups `(...)`, structs, and interfaces
- **Line/block commenting** — `//` and `/* */` toggle; go.mod commenting support
- **Brace matcher** — paired matching for `{}`, `[]`, `()`
- **Smart quote handler** — handles `"`, `` ` ``, `'` (rune literals)
- **Spellcheck** — enabled in comments and string literals
- **Line markers** — green Run gutter icon on `func main()` in `main` packages

### SDK Management
- Auto-detection of installed Go SDKs on project open and IDE start
- Multi-version discovery with `.go-version`, `go.work`, `go.mod`, `GOTOOLCHAIN` support
- Application-level SDK global registry with version caching
- SDK validation (checks for valid Go executable in SDK home)
- Status bar widget showing current Go version with dropdown to switch SDKs
- Status bar actions: Add Local SDK, Download SDK, switch between registered SDKs
- Editor notification banner when Go SDK is not configured for a `.go` file
- SDK download support (fetches available versions and downloads selected)
- Environment caching: `GOOS`, `GOARCH`, `GOPROXY` from selected SDK
- Reads `~/.config/go/env` and system env for `GOPATH`, `GOMODCACHE`, `GOTOOLCHAIN`
- Automatically starts/stops gopls when SDK changes

### LSP Integration (gopls)
- Language server factory spawning `gopls serve` with proper `GOROOT`, `GOPATH`, `GOMODCACHE`, `PATH`
- LSP client with initialization options for semantic tokens
- LSP mappings for Go, GoMod, go.work, and go.sum files
- Parameter info / signature hints
- Structure view (document symbols)
- Real-time error diagnostics
- Code navigation (go to definition, find usages, hover)
- Selection range disabled for go.mod/go.work files
- Optional debug logging of semantic token messages

### Run Configuration
- Run configuration type, factory, and settings editor UI
- Target path, working directory, program arguments, environment variables
- Build modes: DEFAULT, DEBUG, RELEASE
- Cross-compilation: `GOOS`, `GOARCH` configuration
- Advanced options: race detector, build tags, `CGO_ENABLED`, `GOPROXY`, stdin file, external terminal, timeout
- Run producers for files (`main.go`) and packages containing `func main()`
- Program runner with automatic `go build` before execution and build error capture
- Pre-run task providers: `go generate ./...` and `go mod tidy`
- Console output folding for indented setup lines

### Go Modules Support
- go.mod file type with parser, lexer, and syntax highlighter
- Folding for `require(...)` and `exclude(...)` blocks
- Line and block commenting in go.mod files
- go.sum editor notification advising against manual edits
- Module file reconciliation after SDK changes

### Project Wizard
- "Go" option in New Project dialog
- Wizard steps with Git integration and Go-specific SDK path configuration

### File Creation
- New Go file action with templates: Simple file, Main file, Test file
- Auto-generates valid package name from directory name

### Settings
- Application-level settings: LSP enablement, semantic tokens toggle, gopls options
- Project-level settings: Go SDK path selection
- XML-serialized persistence for both scopes
- Message bus notifications and UI listeners on settings change

## Development Workflow

**After every code change, run Kotlin compilation and fix any errors before considering the task done:**

```bash
./gradlew compileKotlin
```

Do not leave compilation errors unfixed. If errors are introduced by a change, resolve them immediately in the same task.

**After every successful change, provide a short one-line commit message** summarising what was done.

## Researching Library APIs

When you need to explore APIs from a dependency (e.g. lsp4ij, IntelliJ Platform), **search the library's GitHub source repository** — do not decompile local jars. Use the WebSearch or WebFetch tools against the relevant GitHub repo.

- lsp4ij: https://github.com/redhat-developer/lsp4ij
- IntelliJ Platform SDK: https://github.com/JetBrains/intellij-community

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
