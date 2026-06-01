package com.github.themirachathumina.golangintellijcommunity.packages

data class GoModuleItem(
    val path: String,
    val version: String,
    val latestVersion: String? = null,
    val indirect: Boolean = false
) {
    val name: String get() = path.substringAfterLast("/")
    val hasUpdate: Boolean get() = latestVersion != null && latestVersion != version
    val pkgDevUrl: String get() = "https://pkg.go.dev/$path"
}
