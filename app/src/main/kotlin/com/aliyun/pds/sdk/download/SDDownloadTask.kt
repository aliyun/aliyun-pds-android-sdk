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

import com.aliyun.pds.sdk.*

class SDDownloadTask(
    taskId: String,
    val fileId: String,
    var fileName: String,
    val fileSize: Long,
    var downloadUrl: String?,
    var filePath: String,
    val driveId: String?,
    val shareId: String?,
    val revisionId: String?,
    val shareToken: String?,
    val contentHash: String? = "",
    val contentHashName: String? = "",
    val isLivePhoto: Boolean = false
) : SDBaseTask(taskId) {

    val resultCheck: ResultCheck =
        (if (contentHashName == "crc64") CRC64Check() else if (contentHashName == "sha1") SHA1Check() else SizeCheck())

    val config = SDClient.instance.config
    val dao = SDClient.instance.database.transferDB.downloadBlockInfoDao()

    override fun start() {
        operation = createOperation(this)
        execute()
    }

    override fun forkTask(): SDTask {
        val newTask = SDDownloadTask(
            taskId,
            fileId,
            fileName,
            fileSize,
            downloadUrl,
            filePath,
            driveId,
            shareId,
            revisionId,
            shareToken,
            contentHash,
            contentHashName,
            isLivePhoto
        )
        newTask.operation = createOperation(newTask)
        newTask.setOnProgressChangeListener(progressListener)
        newTask.setOnCompleteListener(completeListener)
        return newTask
    }

    fun createOperation(task: SDDownloadTask): Operation {
        val ctx = SDClient.instance.appContext
        return if (isLivePhoto) {
            LivePhotoDownloadOperation(ctx, task, dao, config)
        } else {
            DownloadOperation(ctx, task, dao, config, resultCheck)
        }
    }

}