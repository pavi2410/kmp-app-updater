package com.pavi2410.appupdater

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ConfigValidationTest {

    private val noopDownloader = object : AssetDownloader {
        override suspend fun download(url: String, fileName: String, onProgress: (Long, Long) -> Unit) = ""
    }
    private val noopInstaller = object : AssetInstaller {
        override fun install(filePath: String) {}
    }
    private val noopSource = object : UpdateSource {
        override suspend fun fetchReleases() = emptyList<ReleaseInfo>()
    }

    private fun updater(
        version: String = "1.0.0",
        assetMatcher: (String) -> Boolean = { true },
    ) = AppUpdater(version, noopSource, noopDownloader, noopInstaller, assetMatcher)

    private fun source(
        owner: String = "owner",
        repo: String = "repo",
    ) = GitHubUpdateSource(owner, repo)

    @Test
    fun validConfig() {
        val u = updater()
        assertEquals("1.0.0", u.currentVersion)
    }

    @Test
    fun blankOwnerFails() {
        assertFails { source(owner = "") }
    }

    @Test
    fun blankRepoFails() {
        assertFails { source(repo = "") }
    }

    @Test
    fun blankVersionFails() {
        assertFails { updater(version = "") }
    }

    @Test
    fun apiUrlIsCorrect() {
        val s = source(owner = "pavi2410", repo = "kmp-app-updater")
        assertEquals("https://api.github.com/repos/pavi2410/kmp-app-updater/releases", s.apiUrl)
    }

    @Test
    fun customAssetMatcher() {
        val u = updater(assetMatcher = { it.endsWith(".msi") })
        assertEquals(false, u.assetMatcher("app.apk"))
        assertEquals(true, u.assetMatcher("setup.msi"))
    }
}
