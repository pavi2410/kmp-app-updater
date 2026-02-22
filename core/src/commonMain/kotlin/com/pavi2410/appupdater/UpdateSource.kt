package com.pavi2410.appupdater

/**
 * Abstraction for fetching available releases from any update channel
 * (GitHub Releases, GitLab, custom server, etc.).
 */
interface UpdateSource {
    /** Returns available releases, newest first. */
    suspend fun fetchReleases(): List<ReleaseInfo>
}
