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

package com.aliyun.pds.sdk

import android.content.Context
import com.aliyun.pds.sdk.download.DownloadRequestInfo
import com.aliyun.pds.sdk.upload.UploadRequestInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SDTransferClientTest {


    @Mock
    private lateinit var mockContext: Context


    @Before
    fun setup() {
        Mockito.`when`(mockContext.applicationContext).thenReturn(mockContext)
        val config = MockUtils.mockSDConfig()
        SDClient.instance.init(mockContext, config)
    }

    @Test
    fun createDownloadTask() {
        val downloadInfo = DownloadRequestInfo.Builder().downloadUrl("url").build()
        val task = SDClient.instance.createDownloadTask("1", downloadInfo)

        assert(task.downloadUrl == "url")

        task.pause()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.PAUSED)

        task.resume()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.RUNNING)

        task.cancel()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.FINISH)
    }

    @Test
    fun createUploadTask() {

        val uploadInfo = UploadRequestInfo.Builder()
            .driveId("id")
            .build()
        val task = SDClient.instance.createUploadTask("2", uploadInfo)
        assert(task.driveId == "id")

        task.pause()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.PAUSED)

        task.resume()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.RUNNING)

        task.cancel()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.FINISH)
    }

}