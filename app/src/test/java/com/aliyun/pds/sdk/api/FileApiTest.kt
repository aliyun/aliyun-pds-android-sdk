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

import android.content.Context
import com.alibaba.fastjson.JSON
import com.aliyun.pds.sdk.MockUtils
import com.aliyun.pds.sdk.SDClient
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
class FileApiTest {

    lateinit var server: MockWebServer

    @Mock
    private lateinit var mockContext: Context

    private val fileApi = FileApiImpl()

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        SDClient.instance.init(mockContext, MockUtils.mockSDConfig(apiHost = mockHost()))
    }

    private fun mockHost(): String {
        return "http://${server.hostName}:${server.port}"
    }


    @Test
    fun fileGetTest() {

        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(mapOf(
                    "image_media_metadata" to {},
                    "video_media_metadata" to {},
                    "file_id" to "file_id"))).setResponseCode(200)
        // normal
        server.enqueue(response)
        val resp = fileApi.fileGet(FileGetRequest())
        assert(resp?.fileId == "file_id")

        // error
        val errorRespMock =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(mapOf("code" to "error"))).setResponseCode(500)
        server.enqueue(errorRespMock)
        val errorResp = fileApi.fileGet(FileGetRequest())
        assert(errorResp?.errorCode == "error")
    }

    @Test
    fun fileCopyTest() {

        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(mapOf(
                    "async_task_id" to "123",
                    "file_id" to "file_id"))).setResponseCode(200)
        // normal
        server.enqueue(response)
        val resp = fileApi.fileCopy(FileCopyRequest())
        assert(resp?.fileId == "file_id")
        assert(resp?.taskId == "123")

    }

    @Test
    fun fileMoveTest() {

        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(mapOf(
                    "async_task_id" to "123",
                    "file_id" to "file_id"))).setResponseCode(200)
        // normal
        server.enqueue(response)
        val resp = fileApi.fileMove(FileMoveRequest())
        assert(resp?.fileId == "file_id")
        assert(resp?.taskId == "123")

    }

    @Test
    fun fileDeleteTest() {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(mapOf(
                    "async_task_id" to "delete",
                    "file_id" to "delete"))).setResponseCode(200)

        server.enqueue(response)
        val resp = fileApi.fileMove(FileMoveRequest())
        assert(resp?.fileId == "delete")
        assert(resp?.taskId == "delete")

    }

    @Test
    fun fileListTest() {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(mapOf(
                    "next_marker" to "marker", ))).setResponseCode(200)

        server.enqueue(response)
        val resp = fileApi.fileList(FileListRequest())
        assert(resp?.nextMarker == "marker")
    }

    @Test
    fun asyncTaskTest() {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(mapOf(
                    "async_task_id" to "id", ))).setResponseCode(200)

        server.enqueue(response)
        val resp = fileApi.getAsyncTask(AsyncTaskRequest("id"))
        assert(resp?.taskId == "id")
    }
}