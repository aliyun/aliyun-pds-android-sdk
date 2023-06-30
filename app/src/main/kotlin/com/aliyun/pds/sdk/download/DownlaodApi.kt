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

import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.model.FileGetDownloadUrlRequest
import com.aliyun.pds.sdk.model.FileGetDownloadUrlResp
import com.aliyun.pds.sdk.model.GetShareTokenRequest
import okio.IOException

internal class DownloadApi {

    companion object {
        val instance = DownloadApi()
    }

    fun refreshDownloadUrl(task: SDDownloadTask, expireTime: Long): FileGetDownloadUrlResp? {

        val request = FileGetDownloadUrlRequest()
        request.driveId = task.driveId
        request.shareId = task.shareId
        request.fileId = task.fileId
        request.fileName = task.fileName
        request.expireSec = expireTime
        request.revisionId = task.revisionId

        var resp: FileGetDownloadUrlResp? = null
        try {
            resp = SDClient.instance.fileApi.fileGetDownloadUrl(request, task.shareToken)

            // 分享token 过期情况
            if (401 == resp?.code && !task.shareToken.isNullOrEmpty() && !task.shareId.isNullOrEmpty()) {
                val shareTokenRequest = GetShareTokenRequest(task.shareId, task.sharePwd)
                val shareTokenResp = SDClient.instance.shareApi.getShareToken(shareTokenRequest)
                if (shareTokenResp?.code == 200 && !shareTokenResp.shareToken.isNullOrEmpty()) {
                    task.shareToken = shareTokenResp.shareToken
                    resp = SDClient.instance.fileApi.fileGetDownloadUrl(request, task.shareToken)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resp
    }
}