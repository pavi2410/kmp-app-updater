package com.pavi2410.appupdater

/**
 * Platform context — wraps Android's Context on Android, no-op on Desktop.
 * Consumers create this via the platform-specific factory: `PlatformContext(context)` on Android,
 * `PlatformContext()` on Desktop.
 */
expect class PlatformContext

/**
 * Returns the platform-default asset file matcher (e.g. ".apk" on Android, ".msi"/".dmg" on Desktop).
 */
expect fun platformDefaultAssetMatcher(): (String) -> Boolean

/**
 * Creates the platform-default [AssetDownloader] using the given [PlatformContext].
 */
expect fun platformDefaultDownloader(context: PlatformContext): AssetDownloader

/**
 * Creates the platform-default [AssetInstaller] using the given [PlatformContext].
 */
expect fun platformDefaultInstaller(context: PlatformContext): AssetInstaller
