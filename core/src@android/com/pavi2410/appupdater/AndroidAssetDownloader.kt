package com.pavi2410.appupdater

import android.content.Context
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidAssetDownloader(
    private val context: Context,
    private val httpClient: HttpClient = HttpClient(),
) : AssetDownloader {

    override suspend fun download(
        url: String,
        fileName: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        val outputDir = File(context.cacheDir, "app_updates")
        outputDir.mkdirs()
        val outputFile = File(outputDir, fileName)

        httpClient.prepareGet(url) {
            header("User-Agent", "kmp-app-updater")
            onDownload { bytesSentTotal, contentLength ->
                onProgress(bytesSentTotal, contentLength ?: 0L)
            }
        }.execute { response ->
            val channel: ByteReadChannel = response.bodyAsChannel()
            outputFile.outputStream().use { fileOut ->
                val buffer = ByteArray(8192)
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer)
                    if (bytesRead > 0) {
                        fileOut.write(buffer, 0, bytesRead)
                    }
                }
            }
        }

        outputFile.absolutePath
    }
}
