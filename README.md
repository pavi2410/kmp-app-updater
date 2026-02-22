# kmp-app-updater

[![CI](https://github.com/pavi2410/kmp-app-updater/actions/workflows/ci.yml/badge.svg)](https://github.com/pavi2410/kmp-app-updater/actions/workflows/ci.yml)
[![Instrumented Tests](https://github.com/pavi2410/kmp-app-updater/actions/workflows/instrumented.yml/badge.svg)](https://github.com/pavi2410/kmp-app-updater/actions/workflows/instrumented.yml)

A Kotlin Multiplatform library for in-app updates via GitHub Releases. Supports **Android** and **Desktop (JVM)**.

## Features

- Check GitHub Releases for newer versions with semver comparison
- Streaming download with real progress reporting
- Platform-specific installation (APK intent on Android, OS launcher on Desktop)
- Pre-built Compose Multiplatform UI components (optional)
- Configurable asset matching (`.apk`, `.msi`, `.dmg`, etc.)
- Pre-release support

## Modules

| Module | Description |
|--------|-------------|
| `:core` | Headless KMP library — models, GitHub API client, downloader, installer |
| `:compose-ui` | Optional Compose Multiplatform UI — `UpdateCard`, `DownloadProgressIndicator`, `UpdateBanner` |
| `:sample:android` | Android demo app |
| `:sample:desktop` | Desktop demo app |

## Quick Start

### Android

Platform defaults (downloader, installer, asset matcher) are wired automatically via `PlatformContext`:

```kotlin
val platformContext = PlatformContext(applicationContext)

val updater = GitHubUpdater(
    owner = "your-org",
    repo = "your-app",
    currentVersion = platformContext.appVersionName(),
    platformContext = platformContext,
)

// Check → Download → Install
updater.checkForUpdate()
updater.downloadUpdate()   // observe updater.state for progress
updater.installUpdate()
```

### Desktop

```kotlin
val updater = GitHubUpdater(
    owner = "your-org",
    repo = "your-app",
    currentVersion = "1.0.0",
    platformContext = PlatformContext(),
)
```

### Custom / Test

Bring your own downloader and installer — no `PlatformContext` needed:

```kotlin
val updater = GitHubUpdater(
    owner = "your-org",
    repo = "your-app",
    currentVersion = "1.0.0",
    downloader = myDownloader,
    installer = myInstaller,
    assetMatcher = { it.endsWith(".msi") },
)
```

### With Compose UI

```kotlin
UpdateCard(updater = updater)
```

## State Machine

```
Idle → Checking → UpdateAvailable → Downloading(progress) → ReadyToInstall
                → UpToDate
                → Error
```

Observe `updater.state: StateFlow<UpdateState>` for reactive UI updates.

## Android Setup

Add to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

And `res/xml/file_paths.xml`:

```xml
<paths>
    <cache-path name="app_updates" path="app_updates/" />
</paths>
```

## License

Apache 2.0
