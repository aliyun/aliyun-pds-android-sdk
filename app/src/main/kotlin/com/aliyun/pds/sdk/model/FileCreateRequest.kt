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

class FileCreateRequest {


    @JSONField(name = "content_md5")
    var contentMD5: String? = ""

    @JSONField(name = "content_type")
    var contentType: String? = ""

    var name: String = ""

    @JSONField(name = "part_info_list")
    var partInfoList: ArrayList<PartInfo>? = null

    var size: Long = 0

    var type: String? = ""

    @JSONField(name = "check_name_mode")
    var checkNameMode: String? = ""

    @JSONField(name = "content_hash")
    var contentHash: String? = ""

    @JSONField(name = "content_hash_name")
    var contentHashName: String? = ""

    var description: String? = ""

    @JSONField(name = "drive_id")
    var driveId: String? = ""

    @JSONField(name = "share_id")
    var shareId: String? = null

//    @JSONField(name = "encrypt_mode")
//    var encryptMode: String

    @JSONField(name = "file_id")
    var fileId: String? = null

    var hidden: Boolean = false

    var labels: ArrayList<String>? = null

    @JSONField(name = "last_updated_at")
    var lastUpdated: String? = null

    @JSONField(name = "parent_file_id")
    var parentFileId: String? = ""

    @JSONField(name = "pre_hash")
    var preHash: String? = ""

    @JSONField(name = "user_meta")
    var userMeta: String? = ""
}


