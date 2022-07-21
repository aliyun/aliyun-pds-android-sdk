/*
 *  Copyright 2009-2021 Alibaba Cloud All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.aliyun.pds.sdk.utils

import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.SDConfig
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Exception
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and


class FileUtils {

    companion object {
        val instance = FileUtils()
    }

    fun renameByLength(length: Int, sourceName: String): String {
        // too length
        var destName = sourceName
        if (sourceName.length > length) {
            destName = sourceName.substring(
                0,
                length / 2 - 3
            ) + "..." + sourceName.substring(sourceName.length - length / 2)
        }

        return destName
    }

    fun renameByRepeat(parent: String, sourceName: String): String {

        if (!exists(parent, sourceName)) {
            return sourceName
        }
        // has repeat name
        var i = 1
        val fileInfo = parseFileName(sourceName)
        val prefix = fileInfo[0]
        val suffix = fileInfo[1]
        while (i < 9999) {
            val addName = "($i)"
            val newName = prefix + addName + suffix
            if (!exists(parent, newName)) {
                return newName
            }
            i++
        }
        return sourceName + "_" + System.currentTimeMillis()
    }

    fun fileRule1kSA1(path: String): String {
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(path)
            val buffer = ByteArray(1024)
            val digest = MessageDigest.getInstance("SHA-1")
            var numRead = 0
            numRead = inputStream.read(buffer)
            if (numRead > 0) {
                digest.update(buffer, 0, numRead)
            }
            val sha1Bytes: ByteArray = digest.digest()
            return convertHashToString(sha1Bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                }
            }
        }
        return ""
    }

    fun fileSHA1(path: String): String {
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(path)
            val buffer = ByteArray(1024 * 100)
            val digest = MessageDigest.getInstance("SHA-1")
            var numRead = 0
            while (numRead != -1) {
                numRead = inputStream!!.read(buffer)
                if (numRead > 0) digest.update(buffer, 0, numRead)
            }
            val sha1Bytes = digest.digest()
            return convertHashToString(sha1Bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: Exception) {
                }
            }
        }
        return ""
    }

    fun removeDownloadTmp(taskId: String, savePath: String) {
        val tmpDir = File(savePath)
        val tmpFile = File("${tmpDir.path}${File.separator}.${taskId}.tmp")
        if (tmpFile.exists()) {
            tmpFile.delete()
        }
        val dao = SDClient.instance.database.transferDB.downloadBlockInfoDao()
        dao.delete(taskId)
    }

    fun removeUploadTmp(taskId: String) {

        val dir =
            File(SDClient.instance.appContext.filesDir, SDClient.instance.config.uploadDir)
        val tmpFile = File(dir, taskId)
        if (tmpFile.exists()) {
            tmpFile.delete()
        }

    }

    private fun convertHashToString(hashBytes: ByteArray): String {
        var returnVal = ""
        returnVal = hashBytes.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
        return returnVal.uppercase(Locale.getDefault())
    }


    private fun exists(parent: String, fileName: String): Boolean {
        return File(parent, fileName).exists()
    }

    fun parseFileName(fileName: String): Array<String> {
        val index = fileName.lastIndexOf(".")
        var toPrefix: String
        var toSuffix = ""
        if (index == -1) {
            toPrefix = fileName
        } else {
            toPrefix = fileName.substring(0, index)
            toSuffix = fileName.substring(index, fileName.length)
        }
        return arrayOf(toPrefix, toSuffix)
    }

}