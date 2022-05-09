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

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class UploadRequestInfoTest {

    @Before
    fun init() {
    }

    @Test
    fun buildTest() {
        val uploadInfo = UploadRequestInfo.Builder()
            .fileId("fileId")
            .fileName("fileName")
            .filePath("filePath") 
            .fileSize(1000)
            .parentId("parentId")
            .driveId("driveId")
            .shareId("shareId")
            .sharePwd("sharePwd")
            .shareToken("shareToken")
            .mimeType("mimeType")
            .checkNameMode("checkNameMode")
            .build()

        assert(uploadInfo.fileId == "fileId")
        assert(uploadInfo.fileName == "fileName")
        assert(uploadInfo.filePath == "filePath")
        assert(uploadInfo.fileSize == 1000L)
        assert(uploadInfo.parentId == "parentId")
        assert(uploadInfo.driveId == "driveId")
        assert(uploadInfo.shareId == "shareId")
        assert(uploadInfo.sharePwd == "sharePwd")
        assert(uploadInfo.shareToken == "shareToken")
        assert(uploadInfo.mimeType == "mimeType")
        assert(uploadInfo.checkNameMode == "checkNameMode")

    }
}