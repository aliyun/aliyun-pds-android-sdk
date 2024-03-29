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

package com.aliyun.pds.sdk.api

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.SDConfig
import com.aliyun.pds.sdk.http.HTTPUtils
import com.aliyun.pds.sdk.model.*
import okhttp3.Response
import java.io.IOException

/**
 * file doc reference => https://help.aliyun.com/document_detail/175927.html
 */
interface FileApi {

    /**
     *  file upload first step
     */
    fun fileCreate(createRequest: FileCreateRequest, shareToken: String? = ""): FileCreateResp?

    /**
     * get part uploading url
     */
    fun fileGetUploadUrl(getUploadUrlRequest: FileGetUploadUrlRequest, shareToken: String? = ""): FileGetUploadUrlResp?

    /**
     * when all part upload finish, need call this complete file upload
     */
    fun fileComplete(completeRequest: FileCompleteRequest, shareToken: String? = ""): FileInfoResp?

    /**
     * download url will timeout, so you can use this get a new download url
     */
    fun fileGetDownloadUrl(getDownloadUrlRequest: FileGetDownloadUrlRequest, shareToken: String? = ""): FileGetDownloadUrlResp?

    /**
     * get a file detail
     */
    fun fileGet(getRequest: FileGetRequest): FileInfoResp?

    /**
     * can update file meta info, file name, label, description
     */
    fun fileUpdate(updateRequest: FileUpdateRequest): FileInfoResp?

    /**
     * delete file
     */
    fun fileDelete(deleteRequest: FileDeleteRequest): FileDeleteResp?

    /**
     * copy file
     */
    fun fileCopy(fileCopyRequest: FileCopyRequest): FileCopyResp?

    /**
     * move file
     */
    fun fileMove(fileMoveRequest: FileMoveRequest): FileMoveResp?

    /**
     * list file detail, note the response's <p>next_marker</p> it's use to paging obtain
     */
    fun fileList(fileListRequest: FileListRequest): FileListResp?

    /**
     * search file
     */
    fun fileSearch(fileSearchRequest: FileSearchRequest): FileListResp?

    /**
     * some task like delete a many file dir is will a async task you can use this method to
     * check task state
     *
     * @param getAsyncTaskRequest
     */
    fun getAsyncTask(getAsyncTaskRequest: AsyncTaskRequest): AsyncTaskResp?
}

class FileApiImpl : FileApi {

    private val fileCreatePath = "/v2/file/create"
    private val fileUploadUrlPath = "/v2/file/get_upload_url"
    private val fileCompletePath = "/v2/file/complete"

    private val getDownloadUrlPath = "/v2/file/get_download_url"
    private val fileGetPath = "/v2/file/get"
    private val fileUpdatePath = "/v2/file/update"
    private val fileDeletePath = "/v2/file/delete"
    private val fileCopyPath = "/v2/file/copy"
    private val fileMovePath = "/v2/file/move"
    private val fileListPath = "/v2/file/list"
    private val fileSearchPath = "/v2/file/search"
    private val getAsyncTask = "/v2/async_task/get"


    @Throws(IOException::class)
    override fun fileCreate(createRequest: FileCreateRequest, shareToken: String?): FileCreateResp? {
        val headers = mutableMapOf<String, String>()
        if (!shareToken.isNullOrEmpty()) {
            headers["x-share-token"] = shareToken
        }
        return apiPost(fileCreatePath, createRequest, FileCreateResp(), headers)
    }

    @Throws(IOException::class)
    override fun fileGetUploadUrl(getUploadUrlRequest: FileGetUploadUrlRequest, shareToken: String?): FileGetUploadUrlResp? {
        val headers = mutableMapOf<String, String>()
        if (!shareToken.isNullOrEmpty()) {
            headers["x-share-token"] = shareToken
        }
        return apiPost(fileUploadUrlPath, getUploadUrlRequest, FileGetUploadUrlResp(), headers)
    }

    @Throws(IOException::class)
    override fun fileComplete(completeRequest: FileCompleteRequest, shareToken: String?): FileInfoResp? {
        val headers = mutableMapOf<String, String>()
        if (!shareToken.isNullOrEmpty()) {
            headers["x-share-token"] = shareToken
        }
        return apiPost(fileCompletePath, completeRequest, FileInfoResp(), headers)
    }

    @Throws(IOException::class)
    override fun fileGetDownloadUrl(getDownloadUrlRequest: FileGetDownloadUrlRequest, shareToken: String?): FileGetDownloadUrlResp? {
        val headers = mutableMapOf<String, String>()
        if (!shareToken.isNullOrEmpty()) {
            headers["x-share-token"] = shareToken
        }
        return apiPost(getDownloadUrlPath, getDownloadUrlRequest, FileGetDownloadUrlResp(), headers)
    }

    @Throws(IOException::class)
    override fun fileGet(getRequest: FileGetRequest): FileInfoResp? {
        return apiPost(fileGetPath, getRequest, FileInfoResp())
    }

    @Throws(IOException::class)
    override fun fileUpdate(updateRequest: FileUpdateRequest): FileInfoResp? {
        return apiPost(fileUpdatePath, updateRequest, FileInfoResp())
    }

    @Throws(IOException::class)
    override fun fileDelete(deleteRequest: FileDeleteRequest): FileDeleteResp? {
        return apiPost(fileDeletePath, deleteRequest, FileDeleteResp())
    }

    @Throws(IOException::class)
    override fun fileCopy(fileCopyRequest: FileCopyRequest): FileCopyResp? {
        return apiPost(fileCopyPath, fileCopyRequest, FileCopyResp())
    }

    @Throws(IOException::class)
    override fun fileMove(fileMoveRequest: FileMoveRequest): FileMoveResp? {
        return apiPost(fileMovePath, fileMoveRequest, FileMoveResp())
    }

    @Throws(IOException::class)
    override fun fileList(fileListRequest: FileListRequest): FileListResp? {
        return apiPost(fileListPath, fileListRequest, FileListResp())
    }

    @Throws(IOException::class)
    override fun fileSearch(fileSearchRequest: FileSearchRequest): FileListResp? {
        return apiPost(fileSearchPath, fileSearchRequest, FileListResp())
    }

    @Throws(IOException::class)
    override fun getAsyncTask(getAsyncTaskRequest: AsyncTaskRequest): AsyncTaskResp? {
        return apiPost(getAsyncTask, getAsyncTaskRequest, AsyncTaskResp())
    }
}

fun <T : BaseResp> apiPost(path: String, body: Any, t: T, headers: MutableMap<String, String> = mutableMapOf()): T? {
    var resp: Response? = null
    try {
        val host = SDClient.instance.config.apiHost;
        resp = HTTPUtils.instance.apiPost(host, path, JSON.toJSONString(body), headers)
        val respBody: String = resp?.body!!.string()
        val baseResp: T = if (resp?.code < 300) {
            if (respBody.isNullOrEmpty()) {
                t
            } else {
                JSON.parseObject(respBody, t::class.java)
            }
        } else {
            val jsonObject: JSONObject = JSON.parseObject(respBody)
            if (jsonObject != null) {
                t.errorCode = jsonObject.getString("code")
                t.errorMessage = jsonObject.getString("message")
            } else {
                t.errorCode = "${resp?.code}"
                t.errorMessage = resp?.message
            }
            t
        }
        baseResp.code = resp.code
        baseResp.requestId = resp.header("x-ca-request-id")
        return baseResp
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    } finally {
        resp?.close()
    }
    return null
}