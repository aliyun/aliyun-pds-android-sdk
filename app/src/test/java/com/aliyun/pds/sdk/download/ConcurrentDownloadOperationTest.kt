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

import android.content.Context
import com.aliyun.pds.sdk.MockUtils
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.exception.SpaceNotEnoughException
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.util.regex.Pattern


@RunWith(MockitoJUnitRunner::class)
class ConcurrentDownloadOperationTest {


    lateinit var server: MockWebServer

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFile: File

    @Mock
    private lateinit var mockDao: DownloadBlockInfoDao

    val config = MockUtils.mockSDConfig()
    private lateinit var task: SDDownloadTask

    @Before
    fun init() {
        Mockito.`when`(mockFile.freeSpace).thenReturn(1024 * 1024 * 1024)
        Mockito.`when`(mockContext.getExternalFilesDir(null)).thenReturn(mockFile)
        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        SDClient.instance.init(mockContext, config)
        task =  MockUtils.mockDownloadTask(fileSize = 1024)
        server = MockWebServer()
        server.start()
    }


    @Test
    fun spaceCheckTest() {

        // space check

        Mockito.`when`(mockFile.freeSpace).thenReturn(1024 * 1024)

        val operation = DownloadOperation(mockContext, task, mockDao, config)
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
        val file = File(task.filePath)
        val tmpFile = File("${file.path}${File.separator}.${task.taskId}.tmp")
        assert(tmpFile.exists())
    }


    @Test
    fun fileInitTest() {
        val operation = DownloadOperation(mockContext, task, mockDao, config)
        try {
            operation.preAction()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val file = File(task.filePath)
        val tmpFile = File("${file.path}${File.separator}.${task.taskId}.tmp")
        assert(tmpFile.exists())
    }

    @Test
    fun blockTest() {
        val check = CRC64Check()
        var operation = DownloadOperation(mockContext, task, mockDao, config, check)
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
        operation = DownloadOperation(mockContext, task, mockDao, config, check)
        operation.preAction()


        assert(operation.blockList.size == 2)
        assert(operation.blockList[0].start == 0L)
        assert(operation.blockList[0].offset == 0L)
        assert(operation.blockList[0].end == miniBlokcSize)

        task = MockUtils.mockDownloadTask(fileSize = 1024 * 1024 * 25)
        operation = DownloadOperation(mockContext, task, mockDao, config, check)
        operation.preAction()

        assert(operation.blockList.size == 2)
        assert(operation.blockList[1].start == task.fileSize / 2)
        assert(operation.blockList[1].offset == 0L)
        assert(operation.blockList[1].end == task.fileSize)

    }

    @Test
    fun checkDownloadStateTest() {
//        val operation = ConcurrentDownloadOperation(mockContext, task, mockDao, config)
//        operation.blockList = mutableListOf()
//        operation.checkDownloadState()

    }


    @Test
    fun downloadTest() {
//        mockDownloadFile()
//        val url = "http://" + server.hostName + ":" + server.port + "/file/download"
//        val task = MockUtils.mockDownloadTask(fileSize = 1024 * 1024 * 20, fileName = "20M")
//        task.downloadUrl = url
//        val operation = ConcurrentDownloadOperation(mockContext, task, mockDao, config)
//        operation.preAction()
//        operation.download()
//        operation.resultAction()
    }


    @Test
    fun resultActionTest() {

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
                            val file = File("download.20M")
                            if (-1L == end) {
                                end = file.length()
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

}