package oupson.phototaskerplugin.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

fun getBitmap(context: Context, path : String) : Bitmap? {
    val inputStream = openInputStream(context, path) ?: return null
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()
    return bitmap
}

fun openInputStream(context : Context, path : String) : InputStream? {
    val pathUri = Uri.parse(path)
    return when (pathUri.scheme) {
        "content" -> context.contentResolver.openInputStream(pathUri)
        "file" -> FileInputStream(File(pathUri.path!!))
        else -> {
            val file =
                File(path.let { if (!it.startsWith("sdcard")) "sdcard/$it" else it })
            if (file.exists()) {
                FileInputStream(file)
            } else {
                throw FileNotFoundException(file.path)
            }
        }
    }
}