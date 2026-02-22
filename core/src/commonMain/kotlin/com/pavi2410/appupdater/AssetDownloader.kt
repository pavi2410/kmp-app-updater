package com.pavi2410.appupdater

/**
 * Platform-specific interface for downloading release assets.
 * Each platform decides where to store the downloaded file.
 */
interface AssetDownloader {
    /**
     * Downloads the asset from [url] and returns the local file path.
     * Calls [onProgress] with (bytesDownloaded, totalBytes) during download.
     */
    suspend fun download(
        url: String,
        fileName: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
    ): String
}
