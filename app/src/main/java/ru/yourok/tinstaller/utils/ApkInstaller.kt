package ru.yourok.tinstaller.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import ru.yourok.tinstaller.BuildConfig
import java.io.File


object ApkInstaller {
    fun installApplication(context: Context, filePath: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uriFromFile(context, File(filePath)), "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    private fun uriFromFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }
}