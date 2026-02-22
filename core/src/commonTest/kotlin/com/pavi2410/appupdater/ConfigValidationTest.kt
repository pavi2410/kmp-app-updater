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

    private fun updater(
        owner: String = "owner",
        repo: String = "repo",
        version: String = "1.0.0",
        assetMatcher: (String) -> Boolean = { true },
    ) = GitHubUpdater(owner, repo, version, noopDownloader, noopInstaller, assetMatcher)

    @Test
    fun validConfig() {
        val u = updater()
        assertEquals("owner", u.owner)
        assertEquals("repo", u.repo)
        assertEquals("1.0.0", u.currentVersion)
    }

    @Test
    fun blankOwnerFails() {
        assertFails { updater(owner = "") }
    }

    @Test
    fun blankRepoFails() {
        assertFails { updater(repo = "") }
    }

    @Test
    fun blankVersionFails() {
        assertFails { updater(version = "") }
    }

    @Test
    fun apiUrlIsCorrect() {
        val u = updater(owner = "pavi2410", repo = "kmp-app-updater")
        assertEquals("https://api.github.com/repos/pavi2410/kmp-app-updater/releases", u.apiUrl)
    }

    @Test
    fun customAssetMatcher() {
        val u = updater(assetMatcher = { it.endsWith(".msi") })
        assertEquals(false, u.assetMatcher("app.apk"))
        assertEquals(true, u.assetMatcher("setup.msi"))
    }
}
