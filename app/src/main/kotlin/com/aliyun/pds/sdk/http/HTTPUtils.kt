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

package com.aliyun.pds.sdk.http

import com.alibaba.fastjson.JSON
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.exception.DownloadUrl403Exception
import com.aliyun.pds.sdk.exception.SDNetworkException
import com.aliyun.pds.sdk.exception.SDServerException
import com.aliyun.pds.sdk.exception.SDUnknownException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.util.concurrent.TimeUnit
import okio.BufferedSink

import okhttp3.OkHttpClient
import okio.IOException

import java.io.*
import java.util.*


class HTTPUtils {

    companion object {
        val instance = HTTPUtils()
    }


    private val jsonContentType = "application/json;charset=utf-8".toMediaTypeOrNull();

    private val apiHttpClient: OkHttpClient = OkHttpClient.Builder().build()
    private val downloadHttpClient: OkHttpClient =
        OkHttpClient.Builder().protocols(Collections.singletonList(Protocol.HTTP_1_1)).
            connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build()
    private val uploadHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    interface OnTransferChangeListener {
        fun onChange(size: Long)
    }


    @Throws(IOException::class)
    fun apiPost(host: String, path: String, params: MutableMap<String, Any?>): Response? {
        val jsonStr = JSON.toJSONString(params)
        return apiPost(host, path, jsonStr)
    }

    @Throws(IOException::class)
    fun apiPost(host: String, path: String, body: String): Response? {
        val config = SDClient.instance.config
        val url = host + path
        val body = body.toRequestBody(jsonContentType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", config.token.accessToken)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .post(body)
            .build()
        return apiHttpClient.newCall(request).execute()
    }

    @Throws(Exception::class)
    fun downloadData(
        url: String,
        path: String,
        start: Long,
        offset: Long,
        end: Long,
        progressChangeListener: OnTransferChangeListener?,
    ) {
        val downloadEnd = if (end - 1 > 0) end - 1 else ""
        val downloadStart = start + offset
        val rangeHeader = "bytes=$downloadStart-$downloadEnd"
        val request = Request.Builder()
            .addHeader("Range", rangeHeader)
            .url(url)
            .build()

        var response: Response? = null
        try {
            response = downloadHttpClient.newCall(request).execute()
        } catch (e: IOException) {
            if (e is InterruptedIOException) {
                return
            }
            throw SDNetworkException(e.message)
        }

        if (response.code > 300) {

            if (403 == response.code) {
                throw DownloadUrl403Exception("download url timeout")
            } else {
                throw SDServerException(response.code, response.message)
            }
        } else {

            var randomAccessFile: BufferRandomAccessFile? = null
            var inputStream: InputStream? = null
            try {
                val saveFile = File(path)
                val parentFile = saveFile.parentFile
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs()
                }
                inputStream = response.body!!.byteStream()
                randomAccessFile = BufferRandomAccessFile(saveFile)
                randomAccessFile.seek(downloadStart)
                var len: Int
                val buff = ByteArray(64 * 1024 * 1)
                while (inputStream.read(buff)
                        .also { len = it } != -1
                ) {
                    randomAccessFile.write(buff, 0, len)
                    progressChangeListener?.onChange(len.toLong())
                }
                randomAccessFile.flushAndSync()
            } catch (e: Exception) {
                // okio 如果升级需要注意是否还是抛此异常
                if (e !is InterruptedIOException) {
                    e.printStackTrace()
                    throw SDUnknownException(e.message)
                }
            } finally {
                try {
                    randomAccessFile?.close()
                    inputStream?.close()
                    if (null != response) {
                        response.close()
                    }
                } catch (e: Exception) {
                }
            }
        }
    }


    @Throws(Exception::class)
    fun uploadData(
        url: String,
        file: File,
        contentType: String,
        start: Long,
        size: Long,
        listener: OnTransferChangeListener?,
    ): Response? {
        val requestBody = FileUploadProgressRequestBody(file, contentType, size, start, listener)
        var response: Response? = null
        val request = Request.Builder().url(url).put(requestBody).build()
        val call = uploadHttpClient.newCall(request)
        response = call.execute()
        return response
    }
}


private class FileUploadProgressRequestBody constructor(
    val file: File,
    val contentType: String,
    val size: Long,
    val start: Long,
    val listener: HTTPUtils.OnTransferChangeListener?,
) : okhttp3.RequestBody() {

    override fun contentType(): MediaType? {
        return null
    }

    override fun contentLength(): Long {
        return this.size
    }

    @Throws(IOException::class)
    override fun writeTo(bufferedSink: BufferedSink) {
        var result = ByteArray(BUFFER_SIZE)
        var accessFile: RandomAccessFile? = null
        try {
            accessFile = RandomAccessFile(file, "r")
            accessFile.seek(start)
            var readCount: Long = 0
            while (readCount < size) {

                if ((readCount + BUFFER_SIZE) > size) {
                    result = ByteArray((size - readCount).toInt())
                }
                accessFile.read(result)
                bufferedSink.write(result)
                bufferedSink.flush()
                readCount += result.size.toLong()
                listener?.onChange(result.size.toLong())
            }
        } finally {
            try {
                accessFile?.close()
                bufferedSink?.close()
            } catch (e: IOException) {
                if (e !is InterruptedIOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val BUFFER_SIZE: Int = 1024 * 100
    }

}