package com.pavi2410.appupdater

import android.content.Context
import io.ktor.client.*

/**
 * Creates an [AppUpdater] backed by GitHub Releases with platform-default
 * downloader, installer, and asset matcher for Android.
 */
fun AppUpdater.Companion.github(
    context: Context,
    owner: String,
    repo: String,
    currentVersion: String = context.appVersionName(),
    assetMatcher: (String) -> Boolean = { it.endsWith(".apk") },
    includePreReleases: Boolean = false,
    httpClient: HttpClient? = null,
): AppUpdater = AppUpdater(
    currentVersion = currentVersion,
    source = GitHubUpdateSource(owner, repo, includePreReleases, httpClient),
    downloader = AndroidAssetDownloader(context),
    installer = AndroidAssetInstaller(context),
    assetMatcher = assetMatcher,
)

/**
 * Reads the current app version name from PackageManager.
 */
fun Context.appVersionName(): String =
    try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "0.0.0"
    } catch (_: Exception) {
        "0.0.0"
    }
