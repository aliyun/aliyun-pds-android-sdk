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

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class DownloadRequestTest {


    @Before
    fun init() {
    }

    @Test
    fun buildTest() {

        val downloadInfo = DownloadRequestInfo.Builder()
            .downloadUrl("downloadUrl")
            .fileId("fileId")
            .fileName("fileName")
            .filePath("filePath")
            .fileSize(1000)
            .driveId("driveId")
            .contentHash("hash")
            .contentHashName("crc64")
            .shareId("shareId")
            .sharePwd("sharePwd")
            .shareToken("shareToken").build()

        assert(downloadInfo.downloadUrl == "downloadUrl")
        assert(downloadInfo.fileId == "fileId")
        assert(downloadInfo.fileName == "fileName")
        assert(downloadInfo.filePath == "filePath")
        assert(downloadInfo.fileSize == 1000L)
        assert(downloadInfo.driveId == "driveId")
        assert(downloadInfo.contentHash == "hash")
        assert(downloadInfo.contentHashName == "crc64")
        assert(downloadInfo.shareId == "shareId")
        assert(downloadInfo.sharePwd == "sharePwd")
        assert(downloadInfo.shareToken == "shareToken")

    }
}