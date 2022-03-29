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

import android.content.Context
import com.aliyun.pds.sdk.api.FileApiImpl
import com.aliyun.pds.sdk.database.DatabaseHelper
import com.aliyun.pds.sdk.download.SDDownloadTask
import com.aliyun.pds.sdk.upload.SDUploadTask
import com.aliyun.pds.sdk.utils.FileUtils


enum class SDTransferError {
    None,
    Unknown,
    Network,
    FileNotExist,
    SpaceNotEnough,
    SizeExceed,
    PermissionDenied,
    Server,
    RemoteFileNotExist,
}

class SDClient {


    lateinit var config: SDConfig
    lateinit var appContext: Context
    lateinit var database: DatabaseHelper


    val fileApi = FileApiImpl()

    companion object {
        @JvmField
        val instance = SDClient()
    }


    fun init(context: Context, config: SDConfig) {
        this.config = config
        this.appContext = context.applicationContext
        database = DatabaseHelper()
        database.init(appContext, config.databaseName)
        fileApi.host = config.apiHost
    }

    fun updateToken(token: SDToken) {
        if (!token.accessToken.isNullOrEmpty()) {
            config.token = token
        }
    }

    fun setFastUpload(enableFastUpload: Boolean) {
        config.canFastUpload = enableFastUpload
    }

    fun createDownloadTask(
        taskId: String,
        downloadUrl: String?,
        fileId: String,
        driveId: String?,
        fileName: String,
        fileSize: Long,
        filePath: String,
        shareId: String? = "",
        revisionId: String?,
        shareToken: String? = "",
        contentHash: String? = "",
        contentHashName: String? = "",
        completeListener: OnCompleteListener? = null,
        progressListener: OnProgressListener? = null,
    ): SDDownloadTask {
        var tid = taskId
        if (tid.isEmpty()) {
            val timestamp = System.currentTimeMillis()
            tid = "$fileId$timestamp"
        }
        val task = SDDownloadTask(
            tid,
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
            contentHashName
        )

        task.setOnCompleteListener(completeListener)
        task.setOnProgressChangeListener(progressListener)
        task.start()
        return task
    }

    fun createUploadTask(
        taskId: String,
        fileName: String,
        filePath: String,
        fileSize: Long,
        fileId: String?,
        parentId: String,
        mimeType: String?,
        driveId: String?,
        checkNameMode: String = "auto_rename",
        shareId: String? = null,
        completeListener: OnCompleteListener? = null,
        progressListener: OnProgressListener? = null,
    ): SDUploadTask {
        val timestamp = System.currentTimeMillis()
        var tid = taskId
        if (tid.isEmpty()) {
            tid = "$parentId$timestamp"
        }
        val task = SDUploadTask(
            tid,
            fileName,
            filePath,
            fileSize,
            fileId,
            parentId,
            mimeType,
            driveId,
            shareId,
            checkNameMode
        )

        task.setOnCompleteListener(completeListener)
        task.setOnProgressChangeListener(progressListener)
        task.start()
        return task
    }

    fun cleanUploadTask(taskId: String) {
        FileUtils.instance.removeUploadTmp(taskId)
    }

    fun cleanDownloadTask(taskId: String, savePath: String) {
        FileUtils.instance.removeDownloadTmp(taskId, savePath)
    }
}