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

class FileUpdateRequest {

    @JSONField(name = "custom_index_key")
    var customIndexKey: String? = null

    var description: String? = null

    @JSONField(name = "drive_id")
    var driveId: String? = null

    @JSONField(name = "file_id")
    var fileId: String? = null

    var hidden: Boolean? = null

    var labels: ArrayList<String>? = null

    var name: String? = null

    var starred: Boolean? = null

    @JSONField(name = "user_meta")
    var userMeta: String? = null

}
