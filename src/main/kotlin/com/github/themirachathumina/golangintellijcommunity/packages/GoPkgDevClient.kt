package com.github.themirachathumina.golangintellijcommunity.packages

import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/*
 * Searches pkg.go.dev by fetching the HTML search results page and parsing
 * the SearchSnippet blocks. pkg.go.dev has no public JSON API.
 * All calls are blocking and must be made from a background thread.
 */
object GoPkgDevClient {

    private const val SEARCH_URL = "https://pkg.go.dev/search"

    private val detailsCache = ConcurrentHashMap<String, PackageDetails>()
    private val versionsCache = ConcurrentHashMap<String, List<String>>()

    fun getCachedDetails(path: String): PackageDetails? = detailsCache[path]
    fun getCachedVersions(path: String): List<String>? = versionsCache[path]

    data class SearchResult(
        val path: String,
        val version: String,
        val synopsis: String,
        val licenses: List<String> = emptyList()
    ) {
        val name: String get() = path.substringAfterLast("/")
        val pkgDevUrl: String get() = "https://pkg.go.dev/$path"
    }

    fun search(query: String, limit: Int = 20): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val url = "$SEARCH_URL?q=$encodedQuery&limit=$limit"

        return try {
            val conn = URI.create(url).toURL().openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout = 10_000
            // Disable compression so we can read plain text directly
            conn.setRequestProperty("Accept-Encoding", "identity")
            conn.setRequestProperty("User-Agent", "GoLang-IntelliJ-Community-Plugin")

            val response = if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().readText()
            } else {
                return emptyList()
            }
            conn.disconnect()

            parseHtml(response)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseHtml(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        // pkg.go.dev wraps each result in <div class="SearchSnippet">
        val pathPattern     = Regex("""SearchSnippet-header-path">\(([^)]+)\)</span>""")
        val synopsisPattern = Regex("""data-test-id="snippet-synopsis"[^>]*>\s*(.*?)\s*</p>""", RegexOption.DOT_MATCHES_ALL)
        val versionPattern  = Regex("""<strong>(v[\d][^<]*)</strong>\s*published""")
        val licensePattern  = Regex("""data-test-id="snippet-license"[^>]*>(.*?)</span>""", RegexOption.DOT_MATCHES_ALL)

        html.split("""<div class="SearchSnippet"""").drop(1).forEach { snippet ->
            val path     = pathPattern.find(snippet)?.groupValues?.get(1) ?: return@forEach
            val synopsis = synopsisPattern.find(snippet)?.groupValues?.get(1)?.stripTags() ?: ""
            val version  = versionPattern.find(snippet)?.groupValues?.get(1)?.trim() ?: ""
            val license  = licensePattern.find(snippet)?.groupValues?.get(1)?.stripTags() ?: ""

            results.add(SearchResult(
                path = path,
                version = version,
                synopsis = synopsis,
                licenses = if (license.isNotBlank()) listOf(license) else emptyList()
            ))
        }

        return results
    }

    data class PackageDetails(val synopsis: String, val license: String)

    fun fetchDetails(path: String): PackageDetails {
        detailsCache[path]?.let { return it }
        val url = "https://pkg.go.dev/$path"
        return try {
            val conn = URI.create(url).toURL().openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept-Encoding", "identity")
            conn.setRequestProperty("User-Agent", "GoLang-IntelliJ-Community-Plugin")

            val html = if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().readText()
            } else {
                return PackageDetails("", "")
            }
            conn.disconnect()

            val licensePattern = Regex("""data-test-id="UnitHeader-license"[^>]*>\s*([^<]+)\s*<""")
            val license = licensePattern.find(html)?.groupValues?.get(1)?.trim() ?: ""

            val docBlock = Regex("""<div class="UnitDoc">(.*?)</div>""", RegexOption.DOT_MATCHES_ALL)
                .find(html)?.groupValues?.get(1) ?: ""
            val firstPara = Regex("""<p[^>]*>(.*?)</p>""", RegexOption.DOT_MATCHES_ALL)
                .find(docBlock)?.groupValues?.get(1)?.stripTags() ?: ""

            PackageDetails(synopsis = firstPara, license = license).also { detailsCache[path] = it }
        } catch (_: Exception) {
            PackageDetails("", "")
        }
    }

    fun fetchVersions(path: String): List<String> {
        versionsCache[path]?.let { return it }
        // Module proxy requires uppercase letters escaped as "!<lowercase>" e.g. MyModule → !my!module
        val escapedPath = path.replace(Regex("[A-Z]")) { "!" + it.value.lowercase() }
        val url = "https://proxy.golang.org/$escapedPath/@v/list"
        return try {
            val conn = URI.create(url).toURL().openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept-Encoding", "identity")
            conn.setRequestProperty("User-Agent", "GoLang-IntelliJ-Community-Plugin")
            val text = if (conn.responseCode == 200) conn.inputStream.bufferedReader().readText()
                       else return emptyList()
            conn.disconnect()
            text.lines().filter { it.isNotBlank() }.sortedWith(semverDescending()).also { versionsCache[path] = it }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun semverDescending(): Comparator<String> = Comparator { a, b ->
        val aParts = a.removePrefix("v").split("-", limit = 2)[0].split(".").mapNotNull { it.toIntOrNull() }
        val bParts = b.removePrefix("v").split("-", limit = 2)[0].split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(aParts.size, bParts.size)) {
            val diff = (bParts.getOrElse(i) { 0 }) - (aParts.getOrElse(i) { 0 })
            if (diff != 0) return@Comparator diff
        }
        // release versions rank above pre-releases at the same base version
        val aIsRelease = !a.contains("-")
        val bIsRelease = !b.contains("-")
        when {
            aIsRelease && !bIsRelease -> -1
            !aIsRelease && bIsRelease -> 1
            else -> b.compareTo(a)
        }
    }

    private fun String.stripTags(): String =
        replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
}
