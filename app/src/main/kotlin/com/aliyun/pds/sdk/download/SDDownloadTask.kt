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

import com.aliyun.pds.sdk.*

class SDDownloadTask(
    taskId: String,
    val fileId: String,
    var fileName: String,
    val fileSize: Long,
    var downloadUrl: String?,
    var filePath: String,
    val driveId: String?,
    val shareId: String?,
    val revisionId: String?,
    var shareToken: String?,
    val sharePwd: String?,
    val contentHash: String? = "",
    private val contentHashName: String? = ""
) : SDBaseTask(taskId) {

    private val resultCheck = CRC64Check()
    private val dao = SDClient.instance.database.downloadDao
    val config = SDClient.instance.config

    override fun createOperation(): Operation {
        val ctx = SDClient.instance.appContext
        return DownloadOperation(ctx, this, dao, config, resultCheck)
    }
}