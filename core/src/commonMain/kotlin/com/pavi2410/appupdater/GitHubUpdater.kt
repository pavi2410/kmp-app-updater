package com.pavi2410.appupdater

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * In-app updater that checks GitHub Releases for new versions, downloads assets,
 * and triggers platform-native installation.
 *
 * Two constructor patterns:
 * - **Platform**: `GitHubUpdater(owner, repo, version, platformContext)` — uses
 *   platform-default downloader, installer, and asset matcher automatically.
 * - **Custom / test**: `GitHubUpdater(owner, repo, version, downloader, installer)` —
 *   full control, no [PlatformContext] needed.
 */
class GitHubUpdater(
    val owner: String,
    val repo: String,
    val currentVersion: String,
    val downloader: AssetDownloader,
    val installer: AssetInstaller,
    val assetMatcher: (String) -> Boolean = { true },
    val includePreReleases: Boolean = false,
    httpClient: HttpClient? = null,
) {
    /**
     * Platform constructor — auto-wires [AssetDownloader] and [AssetInstaller]
     * for the current platform using [platformContext].
     */
    constructor(
        owner: String,
        repo: String,
        currentVersion: String,
        platformContext: PlatformContext,
        assetMatcher: (String) -> Boolean = platformDefaultAssetMatcher(),
        includePreReleases: Boolean = false,
        httpClient: HttpClient? = null,
    ) : this(
        owner, repo, currentVersion,
        platformDefaultDownloader(platformContext),
        platformDefaultInstaller(platformContext),
        assetMatcher, includePreReleases, httpClient,
    )

    init {
        require(owner.isNotBlank()) { "owner must not be blank" }
        require(repo.isNotBlank()) { "repo must not be blank" }
        require(currentVersion.isNotBlank()) { "currentVersion must not be blank" }
    }

    internal val apiUrl: String
        get() = "https://api.github.com/repos/$owner/$repo/releases"

    private val client: HttpClient = httpClient ?: HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    /**
     * Checks GitHub Releases for an update newer than [currentVersion].
     * Updates [state] to [UpdateState.UpdateAvailable] or [UpdateState.UpToDate].
     * Returns the [ReleaseInfo] if an update is available, null otherwise.
     */
    suspend fun checkForUpdate(): ReleaseInfo? {
        _state.value = UpdateState.Checking
        return try {
            val release = fetchLatestMatchingRelease()
            if (release != null) {
                val asset = release.assets.firstOrNull { assetMatcher(it.name) }
                if (asset != null) {
                    _state.value = UpdateState.UpdateAvailable(release, asset)
                    release
                } else {
                    _state.value = UpdateState.Error("No matching asset found in release ${release.tagName}")
                    null
                }
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

    private suspend fun fetchLatestMatchingRelease(): ReleaseInfo? {
        val url = if (includePreReleases) {
            "$apiUrl?per_page=10"
        } else {
            "$apiUrl/latest"
        }

        val response: HttpResponse = client.get(url) {
            header("Accept", "application/vnd.github+json")
            header("User-Agent", "kmp-app-updater")
        }

        if (includePreReleases) {
            val releases: List<GitHubRelease> = response.body()
            for (ghRelease in releases) {
                if (ghRelease.draft) continue
                val release = ghRelease.toReleaseInfo()
                val hasMatchingAsset = release.assets.any { assetMatcher(it.name) }
                if (hasMatchingAsset && VersionComparator.isNewerVersion(currentVersion, release.version)) {
                    return release
                }
            }
            return null
        } else {
            val ghRelease: GitHubRelease = response.body()
            val release = ghRelease.toReleaseInfo()
            return if (VersionComparator.isNewerVersion(currentVersion, release.version)) {
                release
            } else {
                null
            }
        }
    }

    fun close() {
        client.close()
    }
}
