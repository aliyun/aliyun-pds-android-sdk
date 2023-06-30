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

import com.aliyun.pds.sdk.download.DownloadRequestInfo
import com.aliyun.pds.sdk.download.SDDownloadTask
import com.aliyun.pds.sdk.upload.SDUploadTask
import com.aliyun.pds.sdk.upload.UploadRequestInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SDTransferClientTest : BaseTest() {

    override fun setup() {
        Mockito.`when`(databaseHelper.downloadDao).thenReturn(downloadDao)
        Mockito.`when`(databaseHelper.uploadInfoDao).thenReturn(uploadInfoDao)
    }

    @Test
    fun createDownloadTask() {
        val downloadInfo = DownloadRequestInfo.Builder().downloadUrl("url").build()
        val task = SDClient.instance.createDownloadTask("1", downloadInfo) as SDDownloadTask

        assert(task.downloadUrl == "url")

        task.stop()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.FINISH)

        task.start()
        Thread.sleep(1)
        assert(task.state == SDBaseTask.TaskState.RUNNING)
    }

    @Test
    fun createUploadTask() {

        val uploadInfo = UploadRequestInfo.Builder()
            .driveId("id")
            .build()
        val task = SDClient.instance.createUploadTask("2", uploadInfo) as SDUploadTask
        assert(task.driveId == "id")

        task.stop()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.FINISH)

        task.start()
        Thread.sleep(1000)
        assert(task.state == SDBaseTask.TaskState.RUNNING)
    }

    @Test
    fun cleanUploadTask() {
        val uploadInfo = UploadRequestInfo.Builder().driveId("id").build()
        val task = SDClient.instance.createUploadTask("2", uploadInfo) as SDUploadTask
//        assert(uploadInfoDao.getUploadInfo(task.taskId) != null)
        SDClient.instance.cleanUploadTask(task.taskId)
        Thread.sleep(1000)
        assert(uploadInfoDao.getUploadInfo(task.taskId) == null)
    }

    @Test
    fun cleanDownloadTask() {
        val downloadInfo = DownloadRequestInfo.Builder().downloadUrl("url").build()
        val task = SDClient.instance.createDownloadTask("1", downloadInfo) as SDDownloadTask
//        assert(downloadDao.getTask(task.taskId).isNotEmpty())
        SDClient.instance.cleanDownloadTask(task.taskId, task.filePath)
        Thread.sleep(1000)
        assert(downloadDao.getAll(task.taskId).isEmpty())

    }

}