package com.pavi2410.appupdater

actual class PlatformContext

actual fun platformDefaultAssetMatcher(): (String) -> Boolean = { name ->
    name.endsWith(".msi") ||
        name.endsWith(".exe") ||
        name.endsWith(".dmg") ||
        name.endsWith(".AppImage") ||
        name.endsWith(".deb") ||
        name.endsWith(".rpm") ||
        name.endsWith(".jar")
}

actual fun platformDefaultDownloader(context: PlatformContext): AssetDownloader =
    DesktopAssetDownloader()

actual fun platformDefaultInstaller(context: PlatformContext): AssetInstaller =
    DesktopAssetInstaller()
