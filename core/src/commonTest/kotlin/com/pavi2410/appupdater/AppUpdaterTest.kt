package com.pavi2410.appupdater

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

class AppUpdaterTest {

    private val latestReleaseJson = """
    {
        "tag_name": "v0.3.1",
        "name": "v0.3.1",
        "body": "Tap the floating overlay button",
        "published_at": "2026-02-18T10:00:00Z",
        "prerelease": false,
        "draft": false,
        "assets": [
            {
                "name": "app-debug.apk",
                "browser_download_url": "https://github.com/test/repo/releases/download/v0.3.1/app-debug.apk",
                "size": 20971520,
                "content_type": "application/vnd.android.package-archive"
            }
        ]
    }
    """.trimIndent()

    private fun createMockClient(responseBody: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = responseBody,
                        status = statusCode,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }

    private val fakeDownloader = object : AssetDownloader {
        var lastUrl: String? = null
        override suspend fun download(
            url: String,
            fileName: String,
            onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
        ): String {
            lastUrl = url
            onProgress(100, 100)
            return "/tmp/fake/$fileName"
        }
    }

    private val fakeInstaller = object : AssetInstaller {
        var lastPath: String? = null
        override fun install(filePath: String) {
            lastPath = filePath
        }
    }

    private fun updater(
        version: String = "0.2.0",
        assetMatcher: (String) -> Boolean = { it.endsWith(".apk") },
        includePreReleases: Boolean = false,
        httpClient: HttpClient = createMockClient(latestReleaseJson),
    ) = AppUpdater(
        currentVersion = version,
        source = GitHubUpdateSource("test", "repo", includePreReleases, httpClient),
        downloader = fakeDownloader,
        installer = fakeInstaller,
        assetMatcher = assetMatcher,
    )

    @Test
    fun checkForUpdateFindsNewerVersion() = runTest {
        val updater = updater("0.2.0")
        val result = updater.checkForUpdate()

        assertNotNull(result)
        assertEquals("0.3.1", result.version)
        assertIs<UpdateState.UpdateAvailable>(updater.state.value)
    }

    @Test
    fun checkForUpdateWhenAlreadyUpToDate() = runTest {
        val updater = updater("0.3.1")
        val result = updater.checkForUpdate()

        assertNull(result)
        assertIs<UpdateState.UpToDate>(updater.state.value)
    }

    @Test
    fun checkForUpdateWhenCurrentIsNewer() = runTest {
        val updater = updater("1.0.0")
        val result = updater.checkForUpdate()

        assertNull(result)
        assertIs<UpdateState.UpToDate>(updater.state.value)
    }

    @Test
    fun checkForUpdateNoMatchingAsset() = runTest {
        val updater = updater(assetMatcher = { it.endsWith(".msi") })
        val result = updater.checkForUpdate()

        assertNull(result)
        val state = updater.state.value
        assertIs<UpdateState.UpToDate>(state)
    }

    @Test
    fun checkForUpdateApiError() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respondError(HttpStatusCode.Forbidden)
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val updater = AppUpdater(
            currentVersion = "0.2.0",
            source = GitHubUpdateSource("test", "repo", httpClient = client),
            downloader = fakeDownloader,
            installer = fakeInstaller,
            assetMatcher = { it.endsWith(".apk") },
        )
        val result = updater.checkForUpdate()

        assertNull(result)
        assertIs<UpdateState.Error>(updater.state.value)
    }

    @Test
    fun downloadUpdateTransitionsState() = runTest {
        val updater = updater()
        updater.checkForUpdate()
        assertIs<UpdateState.UpdateAvailable>(updater.state.value)

        val path = updater.downloadUpdate()

        assertNotNull(path)
        assertEquals("/tmp/fake/app-debug.apk", path)
        assertIs<UpdateState.ReadyToInstall>(updater.state.value)
        assertEquals(path, (updater.state.value as UpdateState.ReadyToInstall).filePath)
    }

    @Test
    fun downloadUpdateWithoutCheckReturnsNull() = runTest {
        val updater = updater()
        val path = updater.downloadUpdate()
        assertNull(path)
    }

    @Test
    fun installUpdateCallsInstaller() = runTest {
        val updater = updater()
        updater.checkForUpdate()
        updater.downloadUpdate()
        updater.installUpdate()

        assertEquals("/tmp/fake/app-debug.apk", fakeInstaller.lastPath)
    }

    @Test
    fun resetReturnsToIdle() = runTest {
        val updater = updater()
        updater.checkForUpdate()
        assertIs<UpdateState.UpdateAvailable>(updater.state.value)

        updater.reset()
        assertIs<UpdateState.Idle>(updater.state.value)
    }

    @Test
    fun configValidation() {
        assertFails { updater(version = "") }
        assertFails {
            GitHubUpdateSource("", "repo")
        }
        assertFails {
            GitHubUpdateSource("owner", "")
        }
    }

    @Test
    fun checkForUpdateWithPreReleases() = runTest {
        val releasesJson = """
        [
            {
                "tag_name": "v0.4.0-beta",
                "body": "Beta release",
                "published_at": "2026-03-01T00:00:00Z",
                "prerelease": true,
                "draft": false,
                "assets": [{"name": "app.apk", "browser_download_url": "https://example.com/beta.apk", "size": 100}]
            },
            {
                "tag_name": "v0.3.1",
                "body": "Stable",
                "published_at": "2026-02-18T00:00:00Z",
                "prerelease": false,
                "draft": false,
                "assets": [{"name": "app.apk", "browser_download_url": "https://example.com/stable.apk", "size": 200}]
            }
        ]
        """.trimIndent()

        val updater = updater(includePreReleases = true, httpClient = createMockClient(releasesJson))
        val result = updater.checkForUpdate()

        // Should not pick up "0.4.0-beta" because toIntOrNull() on "0-beta" returns null
        // It will match "0.3.1" as the first valid newer release
        assertNotNull(result)
    }

    @Test
    fun updaterWithInlineUpdateSource() = runTest {
        val fakeSource = object : UpdateSource {
            override suspend fun fetchReleases() = listOf(
                ReleaseInfo(
                    tagName = "v2.0.0",
                    version = "2.0.0",
                    changelog = "Big release",
                    publishedAt = "2026-06-01T00:00:00Z",
                    assets = listOf(ReleaseAsset("app.apk", "https://example.com/app.apk", 500)),
                )
            )
        }

        val updater = AppUpdater(
            currentVersion = "1.0.0",
            source = fakeSource,
            downloader = fakeDownloader,
            installer = fakeInstaller,
            assetMatcher = { it.endsWith(".apk") },
        )

        val result = updater.checkForUpdate()
        assertNotNull(result)
        assertEquals("2.0.0", result.version)
        assertIs<UpdateState.UpdateAvailable>(updater.state.value)
    }

    @Test
    fun fullEndToEndWithMockV99Release() = runTest {
        val v99ReleaseJson = """
        {
            "tag_name": "v99.0.0",
            "name": "v99.0.0",
            "body": "Major milestone release with all the features.",
            "published_at": "2030-01-01T00:00:00Z",
            "prerelease": false,
            "draft": false,
            "assets": [
                {
                    "name": "app-release.apk",
                    "browser_download_url": "https://github.com/test/repo/releases/download/v99.0.0/app-release.apk",
                    "size": 52428800,
                    "content_type": "application/vnd.android.package-archive"
                },
                {
                    "name": "app-setup.msi",
                    "browser_download_url": "https://github.com/test/repo/releases/download/v99.0.0/app-setup.msi",
                    "size": 104857600,
                    "content_type": "application/x-msi"
                }
            ]
        }
        """.trimIndent()

        val mockClient = createMockClient(v99ReleaseJson)
        val updater = AppUpdater(
            currentVersion = "1.0.0",
            source = GitHubUpdateSource("test", "repo", httpClient = mockClient),
            downloader = fakeDownloader,
            installer = fakeInstaller,
            assetMatcher = { it.endsWith(".apk") },
        )

        // 1. Check — should find v99.0.0
        val release = updater.checkForUpdate()
        assertNotNull(release)
        assertEquals("99.0.0", release.version)
        assertEquals("Major milestone release with all the features.", release.changelog)
        val available = updater.state.value
        assertIs<UpdateState.UpdateAvailable>(available)
        assertEquals("app-release.apk", available.asset.name)
        assertEquals(52428800, available.asset.size)

        // 2. Download
        val path = updater.downloadUpdate()
        assertNotNull(path)
        assertEquals("/tmp/fake/app-release.apk", path)
        assertIs<UpdateState.ReadyToInstall>(updater.state.value)

        // 3. Install
        updater.installUpdate()
        assertEquals("/tmp/fake/app-release.apk", fakeInstaller.lastPath)

        // 4. Reset
        updater.reset()
        assertIs<UpdateState.Idle>(updater.state.value)
    }

    @Test
    fun v99ReleaseDesktopAssetMatcher() = runTest {
        val v99ReleaseJson = """
        {
            "tag_name": "v99.0.0",
            "name": "v99.0.0",
            "body": "Desktop release",
            "published_at": "2030-01-01T00:00:00Z",
            "prerelease": false,
            "draft": false,
            "assets": [
                {
                    "name": "app-release.apk",
                    "browser_download_url": "https://github.com/test/repo/releases/download/v99.0.0/app-release.apk",
                    "size": 50000000
                },
                {
                    "name": "app-setup.msi",
                    "browser_download_url": "https://github.com/test/repo/releases/download/v99.0.0/app-setup.msi",
                    "size": 100000000
                }
            ]
        }
        """.trimIndent()

        val mockClient = createMockClient(v99ReleaseJson)
        val updater = AppUpdater(
            currentVersion = "0.5.0",
            source = GitHubUpdateSource("test", "repo", httpClient = mockClient),
            downloader = fakeDownloader,
            installer = fakeInstaller,
            assetMatcher = { it.endsWith(".msi") },
        )

        val release = updater.checkForUpdate()
        assertNotNull(release)
        val state = updater.state.value
        assertIs<UpdateState.UpdateAvailable>(state)
        assertEquals("app-setup.msi", state.asset.name)
        assertEquals(100000000, state.asset.size)
    }
}
