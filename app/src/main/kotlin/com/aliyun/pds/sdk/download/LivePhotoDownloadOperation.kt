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
import com.aliyun.pds.sdk.OnCompleteListener
import com.aliyun.pds.sdk.OnProgressListener
import com.aliyun.pds.sdk.Operation
import com.aliyun.pds.sdk.SDConfig
import com.aliyun.pds.sdk.SDFileMeta
import java.lang.Exception

class LivePhotoDownloadOperation(
    private val context: Context,
    private val task: SDDownloadTask,
    private val blockInfoDao: DownloadBlockInfoDao,
    private val config: SDConfig,
) : Operation {

    lateinit var imageOperation: DownloadOperation
    lateinit var movOperation: DownloadOperation

    override fun execute() {
        preAction()
        downloadAction()
    }

    override fun stop() {
        imageOperation.stop()
        movOperation.stop()
    }

    override fun cancel() {
        imageOperation.cancel()
        movOperation.cancel()
    }

    fun preAction() {

        //todo get streamInfo
        val imageFileSize = 0L
        val movFileSize = 0L

        val imageUrl = ""
        val movUrl = ""

        val imageSavePath = "${task.savePath}/image"
        val movSavePath = "${task.savePath}/mov"

        val imageContentHash = ""
        val movContentHash = ""


        val imageTask = SDDownloadTask(task.taskId + "_img",
            task.fileId,
            "img" + task.fileName,
            imageFileSize,
            imageUrl,
            imageSavePath,
            task.driveId,
            task.shareId,
            imageContentHash,
            task.contentHashName)


        val movTask = SDDownloadTask(task.taskId + "_mov",
            task.fileId,
            "mov" + task.fileName,
            movFileSize,
            movUrl,
            movSavePath,
            task.driveId,
            task.shareId,
            movContentHash,
            task.contentHashName)

        val resultCheck: ResultCheck =
            (if (task.contentHashName == "crc64") CRC64Check() else if (task.contentHashName == "sha1") SHA1Check() else SizeCheck())

        imageOperation = DownloadOperation(context, imageTask, blockInfoDao, config, resultCheck)
        movOperation = DownloadOperation(context, movTask, blockInfoDao, config, resultCheck)

        imageTask.setOnCompleteListener(object : OnCompleteListener {
            override fun onComplete(taskId: String, fileMeta: SDFileMeta, exception: Exception?) {
                if (null != exception) {
                    task.completeListener?.onComplete(taskId, fileMeta, exception)
                } else {
                    movOperation.execute()
                }
            }
        })

        imageTask.setOnProgressChangeListener(object : OnProgressListener {
            override fun onProgressChange(currentSize: Long) {
                task.progressListener?.onProgressChange(currentSize)
            }
        })

        movTask.setOnProgressChangeListener(object: OnProgressListener {
            override fun onProgressChange(currentSize: Long) {
                task.progressListener?.onProgressChange(imageTask.fileSize + currentSize)
            }
        })

        movTask.setOnCompleteListener(object: OnCompleteListener {
            override fun onComplete(taskId: String, fileMeta: SDFileMeta, exception: Exception?) {
                task.completeListener?.onComplete(taskId, fileMeta, exception)
            }
        })
    }

    fun downloadAction() {
        imageOperation.execute()
    }
}
