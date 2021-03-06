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

package com.aliyun.pds.sdk.model

import com.alibaba.fastjson.annotation.JSONField

class FileGetDownloadUrlResp : BaseResp() {
    var expiration: String? = ""
    var method: String? = ""
    var size: Long? = 0
    var url: String? = ""
    @JSONField(name = "streams_info")
    var streamsInfo: Map<String, StreamsInfoItem>? = null
}

class StreamsInfoItem {
    @JSONField(name = "crc64_hash")
    var crc64: String = ""
    var url: String = ""
    var size: Long = 0

}
