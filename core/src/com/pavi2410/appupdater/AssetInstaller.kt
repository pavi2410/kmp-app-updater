package com.pavi2410.appupdater

/**
 * Platform-specific interface for installing downloaded assets.
 */
interface AssetInstaller {
    /**
     * Triggers the platform's native install flow for the file at [filePath].
     */
    fun install(filePath: String)
}
