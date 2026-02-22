package com.pavi2410.appupdater

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidAssetInstallerTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()

    @Test
    fun installThrowsIfFileDoesNotExist() {
        val installer = AndroidAssetInstaller(context)
        try {
            installer.install("/nonexistent/path/app.apk")
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("does not exist"))
        }
    }

    @Test
    fun fileProviderResolvesUri() {
        // Write a dummy file to cache
        val dir = File(context.cacheDir, "app_updates")
        dir.mkdirs()
        val file = File(dir, "test.apk")
        file.writeText("dummy apk content")

        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            assertNotNull(uri)
            assertTrue(uri.toString().contains("fileprovider"))
        } finally {
            file.delete()
        }
    }
}
