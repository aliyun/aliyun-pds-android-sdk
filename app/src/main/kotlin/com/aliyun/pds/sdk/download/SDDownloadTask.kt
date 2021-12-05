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
    fileId: String,
    fileName: String,
    fileSize: Long,
    downloadUrl: String,
    savePath: String,
    driveId: String?,
    shareId: String?,
    crc64Hash: String? = ""
) : SDBaseTask(taskId) {

    val fileId = fileId
    var fileName = fileName
    val fileSize = fileSize
    var downloadUrl = downloadUrl
    val driveId = driveId
    val shareId = shareId
    val savePath = savePath
    val crc64Hash = crc64Hash

    val resultCheck: ResultCheck = CRC64Check()


    override fun start() {
        val dao = SDClient.instance.database.transferDB.downloadBlockInfoDao()
        val config = SDClient.instance.config
        operation =
            DownloadOperation(SDClient.instance.appContext, this, dao, config, resultCheck)
        execute()
        state = TaskState.RUNNING
    }

    override fun forkTask(): SDTask {
        val newTask = SDDownloadTask(
           taskId, fileId, fileName, fileSize, downloadUrl, savePath, shareId, crc64Hash
        )
        val dao = SDClient.instance.database.transferDB.downloadBlockInfoDao()
        val config = SDClient.instance.config
        newTask.operation =
            DownloadOperation(SDClient.instance.appContext, newTask, dao, config, resultCheck)
        newTask.execute()
        // cancel old
        cancel()
        return newTask
    }

}