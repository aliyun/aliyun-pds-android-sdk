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
import com.aliyun.pds.sdk.api.ShareApiImpl
import com.aliyun.pds.sdk.database.DatabaseHelper
import com.aliyun.pds.sdk.download.DownloadRequestInfo
import com.aliyun.pds.sdk.download.SDDownloadTask
import com.aliyun.pds.sdk.upload.SDUploadTask
import com.aliyun.pds.sdk.upload.UploadRequestInfo
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
    ShareLinkCancelled,
    TmpFileNotExist,
    PathRuleError
}

class SDClient {


    lateinit var config: SDConfig
    lateinit var appContext: Context
    lateinit var database: DatabaseHelper


    val fileApi = FileApiImpl()
    val shareApi = ShareApiImpl()

    companion object {
        @JvmField
        val instance = SDClient()
    }


    fun init(context: Context, config: SDConfig) {
        this.config = config
        this.appContext = context.applicationContext
        database = DatabaseHelper()
        database.init(appContext, config.databaseName)
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
        requestInfo: DownloadRequestInfo,
        completeListener: OnCompleteListener? = null,
        progressListener: OnProgressListener? = null,
    ): SDDownloadTask {
        var tid = taskId
        if (tid.isEmpty()) {
            val timestamp = System.currentTimeMillis()
            tid = "${requestInfo.fileId}$timestamp"
        }
        val task = SDDownloadTask(
            tid,
            requestInfo.fileId,
            requestInfo.fileName,
            requestInfo.fileSize,
            requestInfo.downloadUrl,
            requestInfo.filePath,
            requestInfo.driveId,
            requestInfo.shareId,
            requestInfo.revisionId,
            requestInfo.shareToken,
            requestInfo.sharePwd,
            requestInfo.contentHash,
            requestInfo.contentHashName,
            requestInfo.isLivePhoto
        )

        task.setOnCompleteListener(completeListener)
        task.setOnProgressChangeListener(progressListener)
        task.start()
        return task
    }

    fun createUploadTask(
        taskId: String,
        requestInfo: UploadRequestInfo,
        completeListener: OnCompleteListener? = null,
        progressListener: OnProgressListener? = null,
    ): SDUploadTask {
        val timestamp = System.currentTimeMillis()
        var tid = taskId
        if (tid.isEmpty()) {
            tid = "${requestInfo.parentId}$timestamp"
        }
        val task = SDUploadTask(
            tid,
            requestInfo.fileName,
            requestInfo.filePath,
            requestInfo.fileSize,
            requestInfo.fileId,
            requestInfo.parentId,
            requestInfo.mimeType,
            requestInfo.driveId,
            requestInfo.shareId,
            requestInfo.shareToken,
            requestInfo.sharePwd,
            requestInfo.checkNameMode
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