package com.pavi2410.appupdater

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * [UpdateSource] backed by GitHub Releases API.
 *
 * Fetches releases from `https://api.github.com/repos/{owner}/{repo}/releases`.
 */
class GitHubUpdateSource(
    val owner: String,
    val repo: String,
    val includePreReleases: Boolean = false,
    httpClient: HttpClient? = null,
) : UpdateSource {

    init {
        require(owner.isNotBlank()) { "owner must not be blank" }
        require(repo.isNotBlank()) { "repo must not be blank" }
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

    override suspend fun fetchReleases(): List<ReleaseInfo> {
        val url = if (includePreReleases) {
            "$apiUrl?per_page=10"
        } else {
            "$apiUrl/latest"
        }

        val response: HttpResponse = client.get(url) {
            header("Accept", "application/vnd.github+json")
            header("User-Agent", "kmp-app-updater")
        }

        return if (includePreReleases) {
            val releases: List<GitHubRelease> = response.body()
            releases
                .filterNot { it.draft }
                .map { it.toReleaseInfo() }
        } else {
            val ghRelease: GitHubRelease = response.body()
            listOf(ghRelease.toReleaseInfo())
        }
    }

    fun close() {
        client.close()
    }
}
