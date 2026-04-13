package com.pavi2410.appupdater

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.*

class DesktopAssetDownloaderTest {

    @Test
    fun downloadWritesFileToTempDir() = runTest {
        val fileContent = "fake desktop binary content"
        val mockClient = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = fileContent,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentLength, fileContent.length.toString())
                    )
                }
            }
        }

        val downloadDir = File(System.getProperty("java.io.tmpdir"), "kmp-updater-test-${System.currentTimeMillis()}")
        val downloader = DesktopAssetDownloader(downloadDir = downloadDir, httpClient = mockClient)

        var reportedProgress = false
        val path = downloader.download(
            url = "https://example.com/test-app.msi",
            fileName = "test-app.msi",
            onProgress = { downloaded, total ->
                reportedProgress = true
            }
        )

        val file = File(path)
        assertTrue(file.exists(), "Downloaded file should exist")
        assertEquals(fileContent, file.readText())

        // Clean up
        downloadDir.deleteRecursively()
    }

    @Test
    fun downloadCreatesDirectory() = runTest {
        val mockClient = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(content = "x", status = HttpStatusCode.OK)
                }
            }
        }

        val downloadDir = File(System.getProperty("java.io.tmpdir"), "kmp-updater-mkdir-test-${System.currentTimeMillis()}")
        assertFalse(downloadDir.exists())

        val downloader = DesktopAssetDownloader(downloadDir = downloadDir, httpClient = mockClient)
        val path = downloader.download("https://example.com/f.bin", "f.bin") { _, _ -> }

        assertTrue(downloadDir.exists(), "Download dir should be created")
        assertTrue(File(path).exists())

        downloadDir.deleteRecursively()
    }
}
