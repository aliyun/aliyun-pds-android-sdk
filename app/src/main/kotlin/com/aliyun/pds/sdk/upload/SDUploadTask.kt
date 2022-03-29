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

package com.aliyun.pds.sdk.upload

import com.aliyun.pds.sdk.SDBaseTask
import com.aliyun.pds.sdk.SDTask

class SDUploadTask(
    taskId: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    var fileId: String?,
    val parentId: String,
    val mimeType: String?,
    val driveId: String?,
    val shareId: String?,
    val checkNameMode: String?,

    ) : SDBaseTask(taskId) {

    lateinit var sha1: String
    var uploadId: String? = null
    var currentBlock: Int = 0
//    var fileId: String? = null

    enum class UploadState {
        FILE_CREATE, UPLOADING, COMPLETE
    }

    var uploadState = UploadState.FILE_CREATE

    override fun start() {
        operation = UploadOperation(this)
        execute()
    }

    override fun forkTask(): SDTask {
        val newTask = SDUploadTask(
            taskId, fileName, filePath, fileSize, fileId, parentId, mimeType, driveId, shareId, checkNameMode)
        newTask.setOnCompleteListener(completeListener)
        newTask.setOnProgressChangeListener(progressListener)
        return newTask
    }

}