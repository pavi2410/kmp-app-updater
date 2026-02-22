package com.pavi2410.appupdater

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReleaseParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parseSingleRelease() {
        val jsonStr = """
        {
            "tag_name": "v0.3.1",
            "name": "v0.3.1",
            "body": "Tap the floating overlay button to return to the app from anywhere",
            "published_at": "2026-02-18T10:00:00Z",
            "prerelease": false,
            "draft": false,
            "assets": [
                {
                    "name": "app-debug.apk",
                    "browser_download_url": "https://github.com/unitedbyai/droidclaw/releases/download/v0.3.1/app-debug.apk",
                    "size": 20971520,
                    "content_type": "application/vnd.android.package-archive"
                }
            ]
        }
        """.trimIndent()

        val ghRelease = json.decodeFromString<GitHubRelease>(jsonStr)
        val release = ghRelease.toReleaseInfo()

        assertEquals("v0.3.1", release.tagName)
        assertEquals("0.3.1", release.version)
        assertEquals("Tap the floating overlay button to return to the app from anywhere", release.changelog)
        assertEquals("2026-02-18T10:00:00Z", release.publishedAt)
        assertEquals(1, release.assets.size)
        assertEquals("app-debug.apk", release.assets[0].name)
        assertEquals(20971520, release.assets[0].size)
    }

    @Test
    fun parseReleaseWithNoAssets() {
        val jsonStr = """
        {
            "tag_name": "v1.0.0",
            "body": "Initial release",
            "published_at": "2026-01-01T00:00:00Z",
            "prerelease": false,
            "draft": false,
            "assets": []
        }
        """.trimIndent()

        val ghRelease = json.decodeFromString<GitHubRelease>(jsonStr)
        val release = ghRelease.toReleaseInfo()

        assertTrue(release.assets.isEmpty())
    }

    @Test
    fun parseReleaseWithNullBody() {
        val jsonStr = """
        {
            "tag_name": "v1.0.0",
            "published_at": "2026-01-01T00:00:00Z",
            "prerelease": false,
            "draft": false,
            "assets": []
        }
        """.trimIndent()

        val ghRelease = json.decodeFromString<GitHubRelease>(jsonStr)
        val release = ghRelease.toReleaseInfo()

        assertEquals("No changelog available", release.changelog)
    }

    @Test
    fun parseReleasesList() {
        val jsonStr = """
        [
            {
                "tag_name": "v0.3.1",
                "body": "Latest",
                "published_at": "2026-02-18T10:00:00Z",
                "prerelease": false,
                "draft": false,
                "assets": [{"name": "app.apk", "browser_download_url": "https://example.com/app.apk", "size": 100}]
            },
            {
                "tag_name": "v0.2.0",
                "body": "Older",
                "published_at": "2026-01-01T00:00:00Z",
                "prerelease": false,
                "draft": false,
                "assets": [{"name": "app.apk", "browser_download_url": "https://example.com/old.apk", "size": 50}]
            }
        ]
        """.trimIndent()

        val releases = json.decodeFromString<List<GitHubRelease>>(jsonStr)
        assertEquals(2, releases.size)
        assertEquals("v0.3.1", releases[0].tagName)
        assertEquals("v0.2.0", releases[1].tagName)
    }

    @Test
    fun parseReleaseWithMultipleAssetTypes() {
        val jsonStr = """
        {
            "tag_name": "v2.0.0",
            "body": "Multi-platform release",
            "published_at": "2026-03-01T00:00:00Z",
            "prerelease": false,
            "draft": false,
            "assets": [
                {"name": "app-release.apk", "browser_download_url": "https://example.com/app.apk", "size": 100},
                {"name": "app-setup.msi", "browser_download_url": "https://example.com/app.msi", "size": 200},
                {"name": "checksums.txt", "browser_download_url": "https://example.com/checksums.txt", "size": 1}
            ]
        }
        """.trimIndent()

        val ghRelease = json.decodeFromString<GitHubRelease>(jsonStr)
        val release = ghRelease.toReleaseInfo()

        assertEquals(3, release.assets.size)

        // Test asset matcher filtering
        val apkAssets = release.assets.filter { it.name.endsWith(".apk") }
        assertEquals(1, apkAssets.size)
        assertEquals("app-release.apk", apkAssets[0].name)
    }
}
