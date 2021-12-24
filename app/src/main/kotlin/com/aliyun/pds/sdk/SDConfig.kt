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

data class SDConfig(
    var token: SDToken,
    val downloadUrlExpiredTime: Long,
    var apiHost: String,
    val maxRetryCount: Int = 3,
    val canFastUpload: Boolean = true
) {
    companion object {
        // 10M
        const val miniBlock = 1024 * 1024 * 10L
        const val maxBlockCount = 100
        const val uploadDir = "pds/upload"
        const val downloadDir = "pds/download"
    }
}