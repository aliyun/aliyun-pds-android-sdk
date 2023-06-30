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
import com.alibaba.fastjson.JSON
import com.aliyun.pds.sdk.MockUtils
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.database.DatabaseHelper
import com.aliyun.pds.sdk.exception.*
import com.aliyun.pds.sdk.model.FileCreateResp
import com.aliyun.pds.sdk.model.FileGetUploadUrlResp
import com.aliyun.pds.sdk.model.PartInfo
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.sink
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.Charset

@RunWith(MockitoJUnitRunner::class)
class UploadOperationTest {

    @Mock
    private lateinit var mockContext: Context

    lateinit var server: MockWebServer

    @Mock
    private lateinit var uploadMockDao: UploadInfoDao

    @Mock
    private lateinit var databaseHelper: DatabaseHelper


    @Before
    fun setup() {
        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        server = MockWebServer()
        server.start()
        val config = MockUtils.mockSDConfig(apiHost = "http://${server.hostName}:${server.port}")
        SDClient.instance.init(mockContext, config)
        SDClient.instance.database = databaseHelper
    }

    @After
    fun clean() {

    }


    @Test
    fun executeTest() {
        val task = MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024)
        val operation = UploadOperation(task, uploadMockDao)
        operation.execute()
    }

    @Test
    fun intiBlockTest() {

        val task = MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024)
        val operation = UploadOperation(task, uploadMockDao)
        operation.initBlock()
        assert(operation.blockList.size == 4)


        val task1 = MockUtils.mockUploadTask(fileSize = 200 * 1024 * 1024)
        val operation1 = UploadOperation(task1, uploadMockDao)
        operation1.initBlock()
        assert(operation1.blockList.size == 40)

        val task2 = MockUtils.mockUploadTask(fileSize = 2000 * 1024 * 1024)
        val operation2 = UploadOperation(task2, uploadMockDao)
        operation2.initBlock()
        assert(operation2.blockList.size == 400)

        val task3 = MockUtils.mockUploadTask(fileSize = 25 * 1024 * 1024 + 1)
        val operation3 = UploadOperation(task3, uploadMockDao)
        operation3.initBlock()
        assert(operation3.blockList.size == 5)
        print("size => ${operation3.blockList[0].size}")
        assert(operation3.blockList[0].size == 5 * 1024 * 1024L)
        assert(operation3.blockList[4].size == 5 * 1024 * 1024L + 1)

        // init block with file
//        val blockInfoDir =
//            File(SDClient.instance.appContext.filesDir, SDClient.instance.config.uploadDir)
//        val blockInfoFile = File(blockInfoDir, task.taskId)
//
//        blockInfoFile.sink().buffer().writeInt(10).flush()
//        val operation4 = UploadOperation(task, mockDao)
//        operation4.initBlock()
//        blockInfoFile.delete()
//        assert(task.currentBlock == 10)
    }

    @Test
    fun initBlockTest1() {
        val task = MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024, taskId = "fileError")
//        val operation = UploadOperation(task)
//
//        val blockInfoDir =
//            File(SDClient.instance.appContext.filesDir, SDClient.instance.config.uploadDir)
//        val blockInfoFile = File(blockInfoDir, task.taskId)
//
//        blockInfoFile.sink().buffer().writeString("10", Charset.forName("utf-8")).flush()
        val operation4 = UploadOperation(task, uploadMockDao)
        operation4.initBlock()
//        blockInfoFile.delete()
        assert(operation4.blockList.size == 4)
        assert(task.currentBlock == 0)
    }


    @Test
    fun createFileTest() {
        // test rapidUpload
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(mockCreateFileRespRapid()).setResponseCode(201)
        server.enqueue(response)

        // normal upload
        val response1 =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(mockFileInfoData()).setResponseCode(201)
        server.enqueue(response1)

        val task = MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024)
        val file = File(task.filePath)
        if (!file.exists()) {
            file.sink().buffer().writeString("file", Charset.forName("utf-8")).flush()
        }
        val operation = UploadOperation(task, uploadMockDao)
        operation.preAction()
        operation.createFile(true)
        assert(task.uploadState == SDUploadTask.UploadState.FINISH)
        assert(operation.currentSize == task.fileSize)

        operation.initBlock()
        operation.createFile(true)
        assert(task.uploadState == SDUploadTask.UploadState.UPLOADING)
        assert(operation.blockList[0].url == "uploadUrl1")
        assert(operation.blockList[1].url == "uploadUrl2")
    }

    @Test
    fun preHashMatch() {
        val resp = mapOf<String, Any>(
            "code" to "PreHashMatched"
        )

        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(resp)).setResponseCode(500)
        server.enqueue(response)

        val response1 =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(mockFileInfoData()).setResponseCode(201)
        server.enqueue(response1)

        val task = MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024)
        val file = File(task.filePath)
        if (!file.exists()) {
            file.sink().buffer().writeString("file", Charset.forName("utf-8")).flush()
        }
        val operation = UploadOperation(task, uploadMockDao)
        operation.initBlock()
        operation.createFile(true)
        assert(task.uploadState == SDUploadTask.UploadState.UPLOADING)
    }

    @Test
    fun createFileSpaceNotEnough() {
        val resp = mapOf<String, Any>(
            "code" to "QuotaExhausted.Drive"
        )
        try {
            createFileError(resp)
        } catch (e: SDServerException) {
            assert(e.errorCode == "QuotaExhausted.Drive")
        }
    }

    @Test
    fun createFileSizeExceed() {
        val resp = mapOf<String, Any>(
            "code" to "InvalidParameter.SizeExceed"
        )
        try {
            createFileError(resp)
        } catch (e: SDServerException) {
            assert(e.errorCode == "InvalidParameter.SizeExceed")
        }
    }

    @Test
    fun createForbidden() {
        val resp = mapOf<String, Any>(
            "code" to "ForbiddenUpload"
        )
        try {
            createFileError(resp)
        } catch (e: SDServerException) {
            assert(e.errorCode == "ForbiddenUpload")
        }
    }

    @Test
    fun createFileNotFound() {
        val resp = mapOf<String, Any>(
            "code" to "NotFound.File"
        )
        try {
            createFileError(resp)
        } catch (e: SDServerException) {
            assert(e.errorCode == "NotFound.File")
        }
    }

    @Test
    fun createFileUnknownError() {
        val resp = mapOf<String, Any>(
            "code" to "unknown"
        )
        try {
            createFileError(resp)
        } catch (e: Exception) {
            assert(e is SDServerException)
        }
    }

    private fun createFileError(resp: Map<String, Any>) {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(resp)).setResponseCode(500)
        server.enqueue(response)
        val task = MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024)
        val file = File(task.filePath)
        if (!file.exists()) {
            file.sink().buffer().writeString("file", Charset.forName("utf-8")).flush()
        }
        val operation = UploadOperation(task, uploadMockDao)
        operation.createFile(true)
    }

    @Test
    fun uploadActionTest() {
//        val task = MockUtils.mockUploadTask()
//        task.state = SDBaseTask.TaskState.RUNNING
//        task.uploadState = SDUploadTask.UploadState.FILE_CREATE
//        val operation = UploadOperation(task)
//        try {
//            operation.uploadAction()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        task.uploadState = SDUploadTask.UploadState.UPLOADING
//        operation.uploadAction()
//
//        task.uploadState = SDUploadTask.UploadState.COMPLETE
//        operation.uploadAction()
//
//        assert(task.state == SDBaseTask.TaskState.FINISH)
    }


    @Test
    fun uploadingTest() {

        // normal
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(mockUploadUrlResp()).setResponseCode(200)
        server.enqueue(response)
        server.enqueue(response)
        val task = MockUtils.mockUploadTask()
        val operation = UploadOperation(task, uploadMockDao)
        operation.preAction()
        operation.blockList[0].url = "http://${server.hostName}:${server.port}"
        operation.blockList[1].url = "http://${server.hostName}:${server.port}"
        task.uploadId = "uploadId"
        operation.uploading()
        operation.stop(true)
        assert(task.uploadState == SDUploadTask.UploadState.FILE_COMPLETE)

        // empty get upload url
        try {
            server.enqueue(response)
            val task1 =
                MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024, filePath = "notFound.file")
            val operation1 = UploadOperation(task1, uploadMockDao)
            operation1.uploading()
        } catch (e: Exception) {
            // get a empty url
            assert(e is SDServerException)
        }

        // file not found
        try {
            val response1 =
                MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                    .setBody(mockFileInfoData(url1 = "http://${server.hostName}:${server.port}"))
                    .setResponseCode(200)
            //get upload url
            server.enqueue(response1)
            // upload file
            server.enqueue(response)
            val task2 =
                MockUtils.mockUploadTask(fileSize = 20 * 1024 * 1024, filePath = "notFound.file")
            val operation2 = UploadOperation(task2, uploadMockDao)
            operation2.preAction()
            operation2.uploading()
        } catch (e: Exception) {
            assert(e is FileNotFoundException)
        }
    }

    @Test
    fun uploadingComplete() {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(mockFileInfoData()).setResponseCode(200)
        server.enqueue(response)

        val task = MockUtils.mockUploadTask()
        val operation = UploadOperation(task, uploadMockDao)
        operation.uploadComplete()
        assert(true)
    }

    @Test
    fun uploadingCompleteServerError() {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(mockFileInfoData()).setResponseCode(500)
        server.enqueue(response)
        try {
            val task = MockUtils.mockUploadTask()
            val operation = UploadOperation(task, uploadMockDao)
            operation.uploadComplete()
        } catch (e: Exception) {
            assert(e is SDServerException)
        }
    }

    @Test
    fun uploadingCompleteIOError() {
        try {
            val task = MockUtils.mockUploadTask()
            val operation = UploadOperation(task, uploadMockDao)
            operation.uploadComplete()
        } catch (e: Exception) {
            assert(e is IOException)
        }
    }

    fun mockCreateFileRespRapid(): String {
        val resp = FileCreateResp()
        resp.fileId = "fileId"
        resp.rapidUpload = true
        return JSON.toJSONString(resp)
    }

    fun mockUploadUrlResp(): String {
        val resp = FileGetUploadUrlResp()
        resp.fileId = "fileId"
        resp.uploadId = "uploadId"
        return JSON.toJSONString(resp)
    }

    fun mockFileInfoData(url1: String = "uploadUrl1", url2: String = "uploadUrl2"): String {
        val resp = FileCreateResp()
        resp.fileId = "fileId"
        val item1 = PartInfo()
        item1.partNumber = 1
        item1.uploadUrl = url1

        val item2 = PartInfo()
        item2.partNumber = 2
        item2.uploadUrl = url2
        val partInfo = arrayListOf(item1, item2)
        resp.partInfoList = partInfo
        return JSON.toJSONString(resp)
    }


//    private fun mockUploadApi() {
//        val dispatcher = object : Dispatcher() {
//            override fun dispatch(request: RecordedRequest): MockResponse {
//
//                when (request.path) {
//                    "/v2/file/create" -> {
//
//                    }
//                    "/v2/file/get_upload_url" -> {
//
//                    }
//                    "/v2/file/complete" -> {
//
//                    }
//                }
//                return MockResponse().setResponseCode(404)
//            }
//        }
//        server.dispatcher = dispatcher
//    }


}