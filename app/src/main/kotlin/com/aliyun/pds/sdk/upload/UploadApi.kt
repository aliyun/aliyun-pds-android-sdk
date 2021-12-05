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


class UploadApi{

    fun createFile(params: FileCreateRequest): FileCreateResp? {
        var resp: FileCreateResp? = null
        try {
            resp = SDClient.instance.fileApi.fileCreate(params)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resp
    }

    fun getUploadUrl(params: FileGetUploadUrlRequest): FileGetUploadUrlResp? {
        var resp: FileGetUploadUrlResp? = null
        try {
            resp = SDClient.instance.fileApi.fileGetUploadUrl(params)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resp
    }

    fun fileComplete(params: FileCompleteRequest): FileInfoResp? {
        var resp: FileInfoResp? = null
        try {
            resp = SDClient.instance.fileApi.fileComplete(params)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resp
    }
}