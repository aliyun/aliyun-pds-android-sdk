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

import com.alibaba.fastjson.JSON
import com.aliyun.pds.sdk.BaseTest
import com.aliyun.pds.sdk.MockUtils
import com.aliyun.pds.sdk.exception.SDPathRuleErrorException
import com.aliyun.pds.sdk.exception.SDServerException
import com.aliyun.pds.sdk.exception.SpaceNotEnoughException
import com.aliyun.pds.sdk.model.FileGetDownloadUrlResp
import com.aliyun.pds.sdk.model.GetShareTokenResp
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.util.regex.Pattern
import kotlin.concurrent.timer


@RunWith(MockitoJUnitRunner::class)
class DownloadOperationTest : BaseTest() {

    private lateinit var server: MockWebServer

    @Mock
    private lateinit var mockFile: File

    private lateinit var task: SDDownloadTask

    override fun setup() {
        Mockito.`when`(databaseHelper.downloadDao).thenReturn(downloadDao)
        Mockito.`when`(mockFile.freeSpace).thenReturn(1024 * 1024 * 1024)
        Mockito.`when`(mockContext.getExternalFilesDir(null)).thenReturn(mockFile)
        task =  MockUtils.mockDownloadTask(fileSize = 1024)
        server = MockWebServer()
        server.start()
        config.apiHost = "http://${server.hostName}:${server.port}"
    }


    @Test
    fun spaceCheckTest() {

        // space check
        Mockito.`when`(mockFile.freeSpace).thenReturn(1024 * 1024)

        val operation = DownloadOperation(mockContext, task, downloadDao, config)
        try {
            operation.preAction()
        } catch (e: Exception) {
            assert(e is SpaceNotEnoughException)
        }

        Mockito.`when`(mockFile.freeSpace).thenReturn(1024 * 1024 * 1024)
        try {
            operation.preAction()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val file = File(task.filePath).parentFile
        val tmpFile = File("${file.path}${File.separator}.${task.taskId}_${task.fileId}.tmp")
        assert(tmpFile.exists())
    }


    @Test
    fun fileInitTest() {
        val operation = DownloadOperation(mockContext, task, downloadDao, config)
        try {
            operation.preAction()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val file = File(task.filePath).parentFile
        val tmpFile = File("${file.path}${File.separator}.${task.taskId}_${task.fileId}.tmp")
        assert(tmpFile.exists())
    }

    @Test
    fun blockTest() {
        val check = CRC64Check()
        var operation = DownloadOperation(mockContext, task, downloadDao, config, check)
        try {
            operation.preAction()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val miniBlokcSize = 1024 * 1024 * 10L
        assert(operation.blockList.size == 1)
        assert(operation.blockList[0].start == 0L)
        assert(operation.blockList[0].offset == 0L)
        assert(operation.blockList[0].end == 1024L)

        var task = MockUtils.mockDownloadTask(fileSize = 1024 * 1024 * 20)
        operation = DownloadOperation(mockContext, task, downloadDao, config, check)
        operation.preAction()


        assert(operation.blockList.size == 2)
        assert(operation.blockList[0].start == 0L)
        assert(operation.blockList[0].offset == 0L)
        assert(operation.blockList[0].end == miniBlokcSize)

        task = MockUtils.mockDownloadTask(fileSize = 1024 * 1024 * 25)
        operation = DownloadOperation(mockContext, task, downloadDao, config, check)
        operation.preAction()

        assert(operation.blockList.size == 2)
        assert(operation.blockList[1].start == task.fileSize / 2)
        assert(operation.blockList[1].offset == 0L)
        assert(operation.blockList[1].end == task.fileSize)

    }

    @Test
    fun executeTest() {
//        val operation = Mockito.mock(DownloadOperation::class.java)
        val  operation = DownloadOperation(mockContext, task, downloadDao, config)
        operation.execute()
        Thread.sleep(1000 * 5)
    }


    @Test
    fun downloadTest() {
        mockDownloadFile()
        val url = "http://" + server.hostName + ":" + server.port + "/file/download"
        val task = MockUtils.mockDownloadTask()
        val file = File(task.filePath)
        file.delete()
        task.downloadUrl = url
        val operation = DownloadOperation(mockContext, task, downloadDao, config)
        try {
            operation.preAction()
            operation.download()
            operation.resultAction()
        } catch (e: Exception) {
            // no exception
            e.printStackTrace()
           assert(false)
        }
        Thread.sleep(1000 * 2)
//        assert(file.exists())
    }

    @Test
    fun downloadErrorFileName() {
        val errorNameTask = MockUtils.mockDownloadTask(filePath = "a/*/b")
        val operation = DownloadOperation(mockContext, errorNameTask, downloadDao, config)
        try {
            operation.preAction()
        } catch (e: Exception) {
           assert(e is SDPathRuleErrorException)
        }
    }

    @Test
    fun downloadWithStop() {
        mockDownloadFile()
        val url = "http://" + server.hostName + ":" + server.port + "/file/download"
        val task = MockUtils.mockDownloadTask()
        val file = File(task.filePath)
        file.delete()
        task.downloadUrl = url
        val operation = DownloadOperation(mockContext, task, downloadDao, config)
        try {
            operation.preAction()
            // 100ms stop task
            timer(name = "stop", daemon = false, initialDelay = 100, period = 100) {
                operation.stop(false)
                this.cancel()
            }
            operation.download()
//            operation.resultAction()
        } catch (e: Exception) {
            // no exception
            e.printStackTrace()
            assert(false)
        }
        Thread.sleep(1000 * 2)
        val tmpDir = File(task.filePath).parentFile
        val tmpFile = File(tmpDir, ".${task.taskId}_${task.fileId}.tmp")
        assert(!file.exists())
        assert(tmpFile.exists())
    }

    @Test
    fun downloadWithCancel() {
        // 下载过程中调用cancel

        mockDownloadFile()
        val url = "http://" + server.hostName + ":" + server.port + "/file/download"
        val task = MockUtils.mockDownloadTask()
        val file = File(task.filePath)
        file.delete()
        task.downloadUrl = url
        val operation = DownloadOperation(mockContext, task, downloadDao, config)
        try {
            operation.preAction()
            timer(name = "stop", daemon = false, initialDelay = 100, period = 100) {
                operation.stop(true)
                this.cancel()
            }
            operation.download()
//            operation.resultAction()
        } catch (e: Exception) {
            // no exception
            e.printStackTrace()
            assert(false)
        }
        Thread.sleep(1000 * 2)
        val tmpDir = File(task.filePath).parentFile
        val tmpFile = File(tmpDir, ".${task.taskId}_${task.fileId}.tmp")
        assert(!file.exists())
        assert(!tmpFile.exists())

    }

    @Test
    fun downloadWithRename() {

    }


    @Test
    fun resultActionTest() {

    }

    @Test
    fun refreshDownloadUrlTest() {
        val operation = DownloadOperation(mockContext, task, downloadDao, config)
        // normal
        val resp  = FileGetDownloadUrlResp()
        resp.url = "test_url"

        var response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(resp)).setResponseCode(200)
        server.enqueue(response)
        val url = operation.refreshDownloadUrl()
        assert(url == "test_url")
        response = MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("").setResponseCode(200)
        server.enqueue(response)

        try {
            operation.refreshDownloadUrl()
        } catch (e: Exception) {
            assert(e is SDServerException)
        }

        response = MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody("").setResponseCode(500)
        server.enqueue(response)

        try {
            operation.refreshDownloadUrl()
        } catch (e: SDServerException) {
            assert(e.code == 500)
        }
    }

    @Test
    fun downloadApi401Test() {

        val resp401  = FileGetDownloadUrlResp()
        resp401.url = "test_url"
        val response401 = MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(JSON.toJSONString(resp401)).setResponseCode(401)
        task = MockUtils.mockDownloadTask(shareId = "shareId", shareToken = "shareToken")

        val resp200  = FileGetDownloadUrlResp()
        resp200.url = "download_url"
        val response200 = MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(JSON.toJSONString(resp200)).setResponseCode(200)

        val shareResp = GetShareTokenResp()
        shareResp.shareToken = "shareTokenNew"
        val responseShare = MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(JSON.toJSONString(shareResp)).setResponseCode(200)

        server.enqueue(response401)
        server.enqueue(responseShare)
        server.enqueue(response200)

        // 获取下载url 401 情况验证
        val resp = DownloadApi.instance.refreshDownloadUrl(task, 1000L)
        assert(resp?.url == "download_url")
        assert(task.shareToken == "shareTokenNew")
        assert(resp?.code == 200)

    }

    private fun mockDownloadFile() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                when (request.path) {
                    "/file/download" -> {
                        val range = request.getHeader("range")
                        val rangeArray = parseRange(range)
                        if (null != rangeArray) {
                            val start = rangeArray[0]
                            var end = rangeArray[1]
                            val file = File("./pds_test/download.file")
                            if (-1L == end) {
                                end = file.length()
                            } else {
                                end += 1
                            }
                            val byteArray = ByteArray((end - start).toInt())
                            print("start => $start")
                            print("end => $end")
                            val bs = file.source().buffer()
                            bs.skip(start)
                            bs.read(byteArray, 0, byteArray.size)
                            val buffer = Buffer()
                            buffer.write(byteArray)
                            return MockResponse().setBody(buffer).setResponseCode(206)
                        } else {
                            return MockResponse().setResponseCode(400)
                        }
                    }
                }
                return MockResponse().setResponseCode(404)
            }
        }
        server.dispatcher = dispatcher

    }


    fun parseRange(range: String?): Array<Long>? {

        if (null == range) {
            return null
        }
        val pattern = """bytes=(\d+)-(\d+)?"""
        val matcher = Pattern.compile(pattern).matcher(range)
        val rangeArray = arrayOf(0L, 0L)
        if (matcher.find()) {
            rangeArray[0] = matcher.group(1).toLong()
            try {
                rangeArray[1] = matcher.group(2).toLong()
            } catch (e: Exception) {
                rangeArray[1] = -1
            }
            return rangeArray
        }
        return null
    }

    fun deleteFilesInDir(dir: File): Boolean {
        // 确认目录存在并且是一个目录
        if (!dir.exists() || !dir.isDirectory) {
            return false
        }
        // 获取目录下的所有文件并循环删除
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                // 如果文件是一个目录，则递归删除目录中的所有文件
                deleteFilesInDir(file)
            } else {
                // 如果文件是一个普通文件，则直接删除
                file.delete()
            }
        }
        return true
    }

}