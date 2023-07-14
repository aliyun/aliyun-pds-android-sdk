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

package com.aliyun.pds.sdk.http

import android.content.Context
import com.aliyun.pds.sdk.MockUtils
import com.aliyun.pds.sdk.SDClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class HTTPUtilsTest {

    lateinit var server: MockWebServer

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun init() {
        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        SDClient.instance.init(mockContext, MockUtils.mockSDConfig())
        server = MockWebServer()
    }

    @Test
    fun apiPost() {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("test").setResponseCode(200)
        server.enqueue(response)
        server.start()

        val resp = HTTPUtils.instance.apiPost("http://${server.hostName}:${server.port}", "",
            mutableMapOf())
        assert(resp?.code == 200)
    }

    @Test
    fun uploadTest() {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("test").setResponseCode(201)
        server.enqueue(response)
        server.start()
        val url = "http://${server.hostName}:${server.port}"
        val resp = HTTPUtils.instance.uploadData(url, File("./pds_test/download.file"), "", 0, 1024 * 10, null)
        assert(resp?.code == 201)
    }

    @Test
    fun downloadTest() {
        val url = startMockServer(body = "downloadTest")
        HTTPUtils.instance.downloadData(url, "./download.test", 0,0, 12, null)
        val file = File("./download.test")
        val dataStr = file.source().buffer().readUtf8Line()
        file.delete()
        assert(dataStr == "downloadTest")
    }

    private fun startMockServer(body: String = "test"): String {
        val response =
            MockResponse().addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(body).setResponseCode(201)
        server.enqueue(response)
        server.start()
        return "http://${server.hostName}:${server.port}"
    }
}