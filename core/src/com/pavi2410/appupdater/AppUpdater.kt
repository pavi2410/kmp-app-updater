package com.pavi2410.appupdater

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Channel-agnostic in-app updater that checks for new versions, downloads assets,
 * and triggers platform-native installation.
 *
 * Two usage patterns:
 * - **Platform shorthand**: `AppUpdater.github(context, owner, repo)` on Android,
 *   `AppUpdater.github(owner, repo, version)` on Desktop — auto-wires downloader,
 *   installer, and asset matcher for the current platform.
 * - **Custom / test**: use the primary constructor with your own [UpdateSource],
 *   [AssetDownloader], and [AssetInstaller].
 */
class AppUpdater(
    val currentVersion: String,
    val source: UpdateSource,
    val downloader: AssetDownloader,
    val installer: AssetInstaller,
    val assetMatcher: (String) -> Boolean = { true },
) {
    companion object {}

    init {
        require(currentVersion.isNotBlank()) { "currentVersion must not be blank" }
    }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    /**
     * Checks for an update newer than [currentVersion].
     * Updates [state] to [UpdateState.UpdateAvailable] or [UpdateState.UpToDate].
     * Returns the [ReleaseInfo] if an update is available, null otherwise.
     */
    suspend fun checkForUpdate(): ReleaseInfo? {
        _state.value = UpdateState.Checking
        return try {
            val releases = source.fetchReleases()
            val release = releases.firstOrNull { release ->
                release.assets.any { assetMatcher(it.name) } &&
                    VersionComparator.isNewerVersion(currentVersion, release.version)
            }
            if (release != null) {
                val asset = release.assets.first { assetMatcher(it.name) }
                _state.value = UpdateState.UpdateAvailable(release, asset)
                release
            } else {
                _state.value = UpdateState.UpToDate
                null
            }
        } catch (e: Exception) {
            _state.value = UpdateState.Error("Failed to check for updates: ${e.message}", e)
            null
        }
    }

    /**
     * Downloads the asset from the current [UpdateState.UpdateAvailable] state.
     * Updates [state] with download progress and transitions to [UpdateState.ReadyToInstall].
     * Returns the local file path of the downloaded asset.
     */
    suspend fun downloadUpdate(): String? {
        val currentState = _state.value
        if (currentState !is UpdateState.UpdateAvailable) return null

        val asset = currentState.asset
        return try {
            _state.value = UpdateState.Downloading(0f, 0, asset.size)
            val filePath = downloader.download(
                url = asset.downloadUrl,
                fileName = asset.name,
                onProgress = { downloaded, total ->
                    val progress = if (total > 0) downloaded.toFloat() / total else 0f
                    _state.value = UpdateState.Downloading(progress, downloaded, total)
                },
            )
            _state.value = UpdateState.ReadyToInstall(filePath)
            filePath
        } catch (e: Exception) {
            _state.value = UpdateState.Error("Download failed: ${e.message}", e)
            null
        }
    }

    /**
     * Triggers the platform-specific install for the file at [filePath],
     * or uses the path from [UpdateState.ReadyToInstall] if no path is provided.
     */
    fun installUpdate(filePath: String? = null) {
        val path = filePath ?: (_state.value as? UpdateState.ReadyToInstall)?.filePath ?: return
        try {
            installer.install(path)
        } catch (e: Exception) {
            _state.value = UpdateState.Error("Install failed: ${e.message}", e)
        }
    }

    /**
     * Resets state back to [UpdateState.Idle].
     */
    fun reset() {
        _state.value = UpdateState.Idle
    }

    fun close() {
        if (source is GitHubUpdateSource) {
            source.close()
        }
    }
}
