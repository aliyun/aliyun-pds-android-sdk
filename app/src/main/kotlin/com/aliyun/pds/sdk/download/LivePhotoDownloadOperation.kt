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

package com.aliyun.pds.sdk.download

import android.content.Context
import com.aliyun.pds.sdk.*
import com.aliyun.pds.sdk.utils.FileUtils

class LivePhotoDownloadOperation(
    private val context: Context,
    private val task: SDDownloadTask,
    private val blockInfoDao: DownloadBlockInfoDao,
    private val config: SDConfig,
) : Operation {

    lateinit var imageOperation: DownloadOperation
    var movOperation: DownloadOperation? = null

    override fun execute() {

        //todo get streamInfo
        val resp = DownloadApi.instance.refreshDownloadUrl(task, config.downloadUrlExpiredTime);

        var onlyImage = false
        if (null == resp) {
            task.completeListener?.onComplete(
                task.taskId,
                SDFileMeta(task.fileId, task.fileName),
                SDErrorInfo(SDTransferError.Network, "get download url error")
            )
            return
        } else {
            if (!resp.url.isNullOrEmpty()) {
                val onlyImageTask = createTask(
                    task.taskId,
                    task.fileName,
                    task.fileSize,
                    task.savePath,
                    task.downloadUrl,
                    task.contentHash
                )
                onlyImageTask.setOnCompleteListener(task.completeListener)
                onlyImageTask.setOnProgressChangeListener(task.progressListener)
                imageOperation =
                    DownloadOperation(context, onlyImageTask, blockInfoDao, config, CRC64Check())
            } else {
                val nameList = FileUtils.instance.parseFileName(task.fileName)
                var movTask: SDDownloadTask? = null
                var imageTask: SDDownloadTask? = null
                for (stream in resp.streamsInfo?.entries!!) {
                    val savePath = "${task.savePath}/${task.fileName}"
                    if (stream.key == "mov") {
                        movTask = createTask(
                            "${task.taskId}_mov",
                            "${nameList[0]}.mov",
                            stream.value.size,
                            savePath,
                            stream.value.url,
                            stream.value.crc64
                        )
                    } else {
                        imageTask = createTask(
                            "${task.taskId}_img",
                            "${nameList[0]}.${stream.key}",
                            stream.value.size,
                            savePath,
                            stream.value.url,
                            stream.value.crc64
                        )
                    }
                }
                imageOperation =
                    DownloadOperation(context, imageTask!!, blockInfoDao, config, CRC64Check())
                movOperation =
                    DownloadOperation(context, movTask!!, blockInfoDao, config, CRC64Check())

                imageTask.setOnCompleteListener(object : OnCompleteListener {
                    override fun onComplete(taskId: String, fileMeta: SDFileMeta, errorInfo: SDErrorInfo?) {
                        if (null != errorInfo) {
                            task.completeListener?.onComplete(taskId, fileMeta, errorInfo)
                        } else {
                            movOperation?.execute()
                        }
                    }
                })

                imageTask.setOnProgressChangeListener(object : OnProgressListener {
                    override fun onProgressChange(currentSize: Long) {
                        task.progressListener?.onProgressChange(currentSize)
                    }
                })

                movTask.setOnProgressChangeListener(object : OnProgressListener {
                    override fun onProgressChange(currentSize: Long) {
                        task.progressListener?.onProgressChange(imageTask.fileSize + currentSize)
                    }
                })

                movTask.setOnCompleteListener(object : OnCompleteListener {
                    override fun onComplete(taskId: String, fileMeta: SDFileMeta, errorInfo: SDErrorInfo?) {
                        task.completeListener?.onComplete(taskId, fileMeta, errorInfo)
                    }
                })
            }
        }
        imageOperation.execute()
    }

    override fun stop() {
        imageOperation.stop()
        movOperation?.stop()
    }

    override fun cancel() {
        imageOperation.cancel()
        movOperation?.cancel()
    }

    fun createTask(
        taskId: String,
        fileName: String,
        fileSize: Long,
        savePath: String,
        url: String,
        contentHash: String?
    ): SDDownloadTask {
        return SDDownloadTask(
            taskId,
            task.fileId,
            fileName,
            fileSize,
            url,
            savePath,
            task.driveId,
            task.shareId,
            contentHash,
            "crc64"
        )
    }

}
