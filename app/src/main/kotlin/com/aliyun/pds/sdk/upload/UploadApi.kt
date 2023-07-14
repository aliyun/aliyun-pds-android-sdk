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

import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.model.*
import okio.IOException


enum class FunType {
    Create, UploadUrl, Compelte
}

internal class UploadApi(
    val task: SDUploadTask
) {

    fun createFile(params: FileCreateRequest): FileCreateResp? {
        var resp: FileCreateResp? = null
        try {
            resp = SDClient.instance.fileApi.fileCreate(params, task.shareToken)
            if (401 == resp?.code) {
                resp = shareToken401(FunType.Create, params) as FileCreateResp
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resp
    }

    fun getUploadUrl(params: FileGetUploadUrlRequest): FileGetUploadUrlResp? {
        var resp: FileGetUploadUrlResp? = null
        try {
            resp = SDClient.instance.fileApi.fileGetUploadUrl(params, task.shareToken)
            if (401 == resp?.code) {
                resp = shareToken401(FunType.UploadUrl, params) as FileGetUploadUrlResp
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resp
    }

    fun fileComplete(params: FileCompleteRequest): FileInfoResp? {
        var resp: FileInfoResp? = null
        try {
            resp = SDClient.instance.fileApi.fileComplete(params, task.shareToken)
            if (401 == resp?.code) {
                resp = shareToken401(FunType.Compelte, params) as FileInfoResp
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resp
    }


    // todo 使用 retry 实现重试
    private fun shareToken401(type: FunType, params: Any): Any? {
        if (task.shareId.isNullOrEmpty()) {
            return null
        }
        if (!task.shareId.isNullOrEmpty()) {
            val shareTokenRequest = GetShareTokenRequest(task.shareId, task.sharePwd)
            val tokenResp = SDClient.instance.shareApi.getShareToken(shareTokenRequest)
            if (!tokenResp?.shareToken.isNullOrEmpty()) {
                task.shareToken = tokenResp?.shareToken
                val resp = when {
                    FunType.Create == type -> {
                        SDClient.instance.fileApi.fileCreate(
                            params as FileCreateRequest,
                            task.shareToken
                        )
                    }
                    FunType.UploadUrl == type -> {
                        SDClient.instance.fileApi.fileGetUploadUrl(
                            params as FileGetUploadUrlRequest,
                            task.shareToken
                        )
                    }
                    else -> {
                        SDClient.instance.fileApi.fileComplete(
                            params as FileCompleteRequest,
                            task.shareToken
                        )
                    }
                }
                return resp
            }
        }
        return null
    }

}