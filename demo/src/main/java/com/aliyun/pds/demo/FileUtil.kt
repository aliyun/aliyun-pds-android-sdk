package com.aliyun.pds.demo

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {

    fun uriToFileApiQ(context: Context, uri: Uri?): File? {
        var file: File? = null
        if (uri == null) return file
        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            file = File(uri.path)
        } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val contentResolver = context.contentResolver
            val displayName = (System.currentTimeMillis().toString()
                    + "." + MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(uri)))
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val cache = File(context.cacheDir.absolutePath, displayName)
                val fos = FileOutputStream(cache)
                val b = ByteArray(1024)
                while (inputStream!!.read(b) != -1) {
                    fos.write(b) // 写入数据
                }
                file = cache
                fos.close()
                inputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    fun uriToFileApi(context: Context, uri: Uri?): File? {
        var file: File? = null
        if (uri!!.scheme == ContentResolver.SCHEME_FILE) {
            file = File(uri.path)
        } else {
            val projection = arrayOf("_data")
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null) {
                val index = cursor.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    val path = cursor.getString(index)

                    if (!path.isNullOrEmpty()) {
                        file = File(path)
                    }
                }
                cursor.close()
            }
        }
        return file
    }

}