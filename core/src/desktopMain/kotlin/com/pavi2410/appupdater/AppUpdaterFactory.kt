package com.pavi2410.appupdater

import io.ktor.client.*

/**
 * Creates an [AppUpdater] backed by GitHub Releases with platform-default
 * downloader, installer, and asset matcher for Desktop JVM.
 */
fun AppUpdater.Companion.github(
    owner: String,
    repo: String,
    currentVersion: String,
    assetMatcher: (String) -> Boolean = { name ->
        name.endsWith(".msi") ||
            name.endsWith(".exe") ||
            name.endsWith(".dmg") ||
            name.endsWith(".AppImage") ||
            name.endsWith(".deb") ||
            name.endsWith(".rpm") ||
            name.endsWith(".jar")
    },
    includePreReleases: Boolean = false,
    httpClient: HttpClient? = null,
): AppUpdater = AppUpdater(
    currentVersion = currentVersion,
    source = GitHubUpdateSource(owner, repo, includePreReleases, httpClient),
    downloader = DesktopAssetDownloader(),
    installer = DesktopAssetInstaller(),
    assetMatcher = assetMatcher,
)
