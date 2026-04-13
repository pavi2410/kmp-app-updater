package com.pavi2410.appupdater

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val prerelease: Boolean = false,
    val draft: Boolean = false,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long = 0,
    @SerialName("content_type") val contentType: String? = null,
)

data class ReleaseInfo(
    val tagName: String,
    val version: String,
    val changelog: String,
    val publishedAt: String,
    val assets: List<ReleaseAsset>,
)

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val size: Long,
)

internal fun GitHubRelease.toReleaseInfo(): ReleaseInfo = ReleaseInfo(
    tagName = tagName,
    version = tagName.removePrefix("v"),
    changelog = body ?: "No changelog available",
    publishedAt = publishedAt ?: "",
    assets = assets.map { it.toReleaseAsset() },
)

internal fun GitHubAsset.toReleaseAsset(): ReleaseAsset = ReleaseAsset(
    name = name,
    downloadUrl = browserDownloadUrl,
    size = size,
)
