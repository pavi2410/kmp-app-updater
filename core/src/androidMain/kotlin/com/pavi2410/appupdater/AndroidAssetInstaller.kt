package com.pavi2410.appupdater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

class AndroidAssetInstaller(
    private val context: Context,
    private val fileProviderAuthority: String = "${context.packageName}.fileprovider",
) : AssetInstaller {

    override fun install(filePath: String) {
        val file = File(filePath)
        require(file.exists()) { "APK file does not exist: $filePath" }

        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, fileProviderAuthority, file)
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }
}
