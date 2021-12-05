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

package com.aliyun.pds.sdk

import com.aliyun.pds.sdk.download.SDDownloadTask
import com.aliyun.pds.sdk.upload.SDUploadTask

object MockUtils {


    fun mockSDConfig(apiHost: String = "apiHost"): SDConfig {
        return SDConfig(
            SDToken("token", 3600),
            3600,
            apiHost,
            3,
        )
    }

    fun mockUploadTask(
        taskId: String = "567",
        fileName: String = "fileName",
        fileId: String = "fileId",
        fileSize: Long = 2000,
        parentId: String = "parentId",
        filePath: String = "pds/filePath",
        mimeType: String = "mp4",
        driveId: String = "driveId",
        ): SDUploadTask {

        return SDUploadTask(taskId, fileName, filePath, fileSize, parentId, mimeType, driveId, null)
    }

    fun mockDownloadTask(
        taskId: String = "123",
        fileId: String = "fileId",
        fileSize: Long = 1000,
        fileName: String = "name",
        downloadUrl: String = "url",
        savePath: String = "savePath",
        driveId: String = "driveId",
        shareId: String = "shareId",
    ): SDDownloadTask {
        return SDDownloadTask(
            taskId,
            fileId,
            fileName,
            fileSize,
            downloadUrl,
            savePath,
            driveId,
            shareId
        )
    }

//    fun mockUploadTask
}