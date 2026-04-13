package com.pavi2410.appupdater

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpdateAvailable(val release: ReleaseInfo, val asset: ReleaseAsset) : UpdateState
    data object UpToDate : UpdateState
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : UpdateState
    data class ReadyToInstall(val filePath: String) : UpdateState
    data class Error(val message: String, val cause: Throwable? = null) : UpdateState
}
