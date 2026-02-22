package com.pavi2410.appupdater

import java.awt.Desktop
import java.io.File

class DesktopAssetInstaller : AssetInstaller {

    override fun install(filePath: String) {
        val file = File(filePath)
        require(file.exists()) { "File does not exist: $filePath" }

        val os = System.getProperty("os.name").lowercase()
        when {
            os.contains("win") -> {
                ProcessBuilder("cmd", "/c", "start", "", file.absolutePath)
                    .start()
            }
            os.contains("mac") -> {
                ProcessBuilder("open", file.absolutePath)
                    .start()
            }
            os.contains("nux") || os.contains("nix") -> {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                } else {
                    ProcessBuilder("xdg-open", file.absolutePath)
                        .start()
                }
            }
            else -> {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                } else {
                    error("Unsupported OS: $os")
                }
            }
        }
    }
}
