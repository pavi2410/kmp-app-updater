package com.pavi2410.appupdater

import android.content.Context

actual class PlatformContext(val context: Context)

actual fun platformDefaultAssetMatcher(): (String) -> Boolean = { it.endsWith(".apk") }

actual fun platformDefaultDownloader(context: PlatformContext): AssetDownloader =
    AndroidAssetDownloader(context.context)

actual fun platformDefaultInstaller(context: PlatformContext): AssetInstaller =
    AndroidAssetInstaller(context.context)

/**
 * Reads the current app version from PackageManager.
 */
fun PlatformContext.appVersionName(): String =
    try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
    } catch (_: Exception) {
        "0.0.0"
    }
