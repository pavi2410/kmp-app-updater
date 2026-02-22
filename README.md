# kmp-app-updater

[![CI](https://github.com/pavi2410/kmp-app-updater/actions/workflows/ci.yml/badge.svg)](https://github.com/pavi2410/kmp-app-updater/actions/workflows/ci.yml)
[![Instrumented Tests](https://github.com/pavi2410/kmp-app-updater/actions/workflows/instrumented.yml/badge.svg)](https://github.com/pavi2410/kmp-app-updater/actions/workflows/instrumented.yml)

A Kotlin Multiplatform library for in-app updates. Supports **Android** and **Desktop (JVM)**.

## Features

- Pluggable update sources — GitHub Releases out of the box, bring your own for GitLab / custom servers
- Streaming download with real-time progress via `StateFlow<UpdateState>`
- Platform-specific installation (APK intent on Android, OS launcher on Desktop)
- Pre-built Compose Multiplatform UI components (optional)
- Configurable asset matching (`.apk`, `.msi`, `.dmg`, etc.)
- Pre-release support

## Modules

| Module | Description |
|--------|-------------|
| `:core` | Headless KMP library — `AppUpdater`, `UpdateSource`, downloader, installer |
| `:compose-ui` | Optional Compose Multiplatform UI — `UpdateCard`, `DownloadProgressIndicator`, `UpdateBanner` |
| `:sample:android` | Android demo app |
| `:sample:desktop` | Desktop demo app |

## Quick Start

### Android

Platform defaults (downloader, installer, asset matcher) are wired automatically:

```kotlin
val updater = AppUpdater.github(
    context = applicationContext,
    owner = "your-org",
    repo = "your-app",
    // currentVersion auto-detected from PackageManager
)

// Check → Download → Install
updater.checkForUpdate()
updater.downloadUpdate()   // observe updater.state for progress
updater.installUpdate()
```

### Desktop

```kotlin
val updater = AppUpdater.github(
    owner = "your-org",
    repo = "your-app",
    currentVersion = "1.0.0",
)
```

### Custom Update Source

Implement `UpdateSource` to connect any backend:

```kotlin
val updater = AppUpdater(
    currentVersion = "1.0.0",
    source = object : UpdateSource {
        override suspend fun fetchReleases() = listOf(/* your releases */)
    },
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

```kotlin
// Compose
val state by updater.state.collectAsState()
when (state) {
    is UpdateState.Downloading -> DownloadProgressIndicator(state.progress, ...)
    // ...
}

// Coroutines
updater.state.collect { state -> /* react */ }
```

## Background Updates

### Android (WorkManager)

Use [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) to check for updates periodically in the background:

```kotlin
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val updater = AppUpdater.github(
            context = applicationContext,
            owner = "your-org",
            repo = "your-app",
        )
        val release = updater.checkForUpdate()
        if (release != null) {
            // Show a notification prompting the user to update
        }
        return Result.success()
    }
}

// Schedule periodic checks (e.g. once every 24 hours)
val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(24, TimeUnit.HOURS)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "update_check",
    ExistingPeriodicWorkPolicy.KEEP,
    request,
)
```

### Desktop (Coroutine Timer)

On Desktop JVM, use a coroutine-based timer to poll for updates:

```kotlin
fun startBackgroundUpdateCheck(updater: AppUpdater, scope: CoroutineScope) {
    scope.launch {
        while (isActive) {
            updater.checkForUpdate()
            delay(24.hours)
        }
    }
}
```

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
