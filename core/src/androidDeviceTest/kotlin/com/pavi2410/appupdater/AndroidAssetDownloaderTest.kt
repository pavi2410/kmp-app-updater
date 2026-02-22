package com.pavi2410.appupdater

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidAssetDownloaderTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private lateinit var mockServer: MockWebServer

    @Before
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun downloadWritesFileToCache() = runTest {
        val fileContent = "fake apk binary content for testing"
        mockServer.enqueue(
            MockResponse()
                .setBody(fileContent)
                .setHeader("Content-Length", fileContent.length.toString())
        )

        val downloader = AndroidAssetDownloader(context)
        var lastDownloaded = 0L
        var lastTotal = 0L

        val path = downloader.download(
            url = mockServer.url("/test.apk").toString(),
            fileName = "test-download.apk",
            onProgress = { downloaded, total ->
                lastDownloaded = downloaded
                lastTotal = total
            }
        )

        val file = File(path)
        assertTrue("Downloaded file should exist", file.exists())
        assertEquals("File content should match", fileContent, file.readText())

        // Clean up
        file.delete()
    }

    @Test
    fun downloadCreatesOutputDirectory() = runTest {
        mockServer.enqueue(MockResponse().setBody("test"))

        // Ensure directory doesn't exist
        val dir = File(context.cacheDir, "app_updates")
        dir.deleteRecursively()

        val downloader = AndroidAssetDownloader(context)
        val path = downloader.download(
            url = mockServer.url("/test.apk").toString(),
            fileName = "mkdir-test.apk",
            onProgress = { _, _ -> }
        )

        assertTrue("Output dir should be created", dir.exists())

        // Clean up
        File(path).delete()
    }
}
