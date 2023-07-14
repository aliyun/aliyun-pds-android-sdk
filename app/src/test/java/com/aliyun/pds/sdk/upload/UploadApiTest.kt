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

import android.content.Context
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.aliyun.pds.sdk.MockUtils
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.database.DatabaseHelper
import com.aliyun.pds.sdk.model.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class UploadApiTest {

    lateinit var server: MockWebServer

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var databaseHelper: DatabaseHelper


    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        SDClient.instance.init(mockContext, MockUtils.mockSDConfig(apiHost = mockHost()))
        SDClient.instance.database = databaseHelper
    }

    private fun mockServer(code: Int, body: String) {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(body).setResponseCode(code)
        server.enqueue(response)
    }

    @Test
    fun createFile() {
        val fileInfo = FileCreateResp()
        fileInfo.driveId = "createFile"
        mockServer(201, JSONObject.toJSONString(fileInfo))
        val uploadApi = UploadApi(MockUtils.mockUploadTask())
        var resp = uploadApi.createFile(FileCreateRequest())
        assert(resp?.driveId == "createFile")

        //error case
        var errorResp = mapOf("code" to "error", "message" to "msg")
        mockServer(500, JSONObject.toJSONString(errorResp))
        resp = uploadApi.createFile(FileCreateRequest())
        assert(resp?.code == 500)
        assert(resp?.errorCode == "error")
        assert((resp?.errorMessage == "msg"))


        mockServer(500, "test")
        try {
            val errorResp = uploadApi.createFile(FileCreateRequest())
        } catch (e: Exception) {
            assert(e is JSONException)
        }
    }

    @Test
    fun getUploadUrl() {
        val fileInfo = FileGetUploadUrlResp()
        val part = PartInfo()
        part.uploadUrl = "url"
        part.partNumber = 1
        fileInfo.partInfoList = arrayListOf(part)

        mockServer(200, JSONObject.toJSONString(fileInfo))
        val uploadApi = UploadApi(MockUtils.mockUploadTask())
        val resp = uploadApi.getUploadUrl(FileGetUploadUrlRequest())
        assert(resp?.code == 200)
        assert(resp?.partInfoList?.get(0)?.partNumber == 1L)
        assert(resp?.partInfoList?.get(0)?.uploadUrl == "url")

        var errorResp = mapOf("code" to "error", "message" to "msg")
        mockServer(500, JSONObject.toJSONString(errorResp))
        val resp1 = uploadApi.getUploadUrl(FileGetUploadUrlRequest())
        assert(resp1?.code == 500)
        assert(resp1?.errorCode == "error")
        assert((resp1?.errorMessage == "msg"))

        mockServer(500, "test")
        try {
            val errorResp = uploadApi.getUploadUrl(FileGetUploadUrlRequest())
        } catch (e: Exception) {
            assert(e is JSONException)
        }
    }


    @Test
    fun fileComplete() {

        val fileInfo = FileInfoResp()
        fileInfo.driveId = "fileComplete"
        mockServer(200, JSONObject.toJSONString(fileInfo))
        val uploadApi = UploadApi(MockUtils.mockUploadTask())
        val resp = uploadApi.fileComplete(FileCompleteRequest())
        assert(resp?.code == 200)
        assert(resp?.driveId == "fileComplete")

        var errorResp = mapOf("code" to "error", "message" to "msg")
        mockServer(500, JSONObject.toJSONString(errorResp))
        val resp1 = uploadApi.fileComplete(FileCompleteRequest())
        assert(resp1?.code == 500)
        assert(resp1?.errorCode == "error")
        assert((resp1?.errorMessage == "msg"))

        mockServer(500, "test")
        try {
            val errorResp = uploadApi.fileComplete(FileCompleteRequest())
        } catch (e: Exception) {
            assert(e is JSONException)
        }
    }

    private fun mockHost(): String {
        return "http://${server.hostName}:${server.port}"
    }
}