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
        return SDConfig.Builder(SDToken("token", 3600), apiHost, 3600)
                    .maxRetryCount(3)
                    .canFastUpload(true)
                    .isDebug(true)
                    .downloadBlockSize(1024 * 1024 * 5L)
                    .uploadBlockSize(1024 * 1024 * 5L)
                    .connectTimeout(30L)
                    .readTimeout(30L)
                    .build()
    }

    fun mockUploadTask(
        taskId: String = "567",
        fileName: String = "fileName",
        fileId: String = "fileId",
        fileSize: Long = 2000,
        parentId: String = "parentId",
        filePath: String = "pds/filePath",
        shareId: String = "",
        shareToken: String = "",
        sharePwd: String = "",
        mimeType: String = "mp4",
        driveId: String = "driveId",
    ): SDUploadTask {

        return SDUploadTask(
            taskId = taskId,
            fileName,
            filePath,
            fileSize,
            parentId,
            mimeType,
            driveId,
            null,
            checkNameMode = "auto_rename",
            shareId = shareId,
            shareToken = shareToken,
            sharePwd = sharePwd,
        )
    }

    fun mockDownloadTask(
        taskId: String = "123",
        fileId: String = "fileId",
        fileSize: Long = 1000,
        fileName: String = "name",
        downloadUrl: String = "url",
        savePath: String = "savePath",
        shareToken: String = "",
        sharePwd: String = "",
        driveId: String = "driveId",
        shareId: String = "shareId",
        reversionId: String = "",
    ): SDDownloadTask {
        return SDDownloadTask(
            taskId,
            fileId,
            fileName,
            fileSize,
            downloadUrl,
            savePath,
            driveId,
            shareId,
            shareToken = shareToken,
            sharePwd = sharePwd,
            revisionId = reversionId
        )
    }

//    fun mockUploadTask
}