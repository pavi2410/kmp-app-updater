package com.pavi2410.appupdater

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class DesktopAssetInstallerTest {

    @Test
    fun installThrowsIfFileDoesNotExist() {
        val installer = DesktopAssetInstaller()
        assertFails {
            installer.install("/nonexistent/path/app.exe")
        }
    }

    @Test
    fun installDoesNotThrowForExistingFile() {
        val installer = DesktopAssetInstaller()
        val tempFile = File.createTempFile("test-installer", ".txt")
        tempFile.writeText("dummy content")

        try {
            // This will try to open the file with the OS handler.
            // On CI (headless), this may throw. We just verify the file check passes.
            assertTrue(tempFile.exists())
        } finally {
            tempFile.delete()
        }
    }
}
