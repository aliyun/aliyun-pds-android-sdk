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

class FileCreateResp : BaseResp() {

    @JSONField(name = "domain_id")
    var domainId: String? = ""

    @JSONField(name = "drive_id")
    var driveId: String? = ""

    @JSONField(name = "file_id")
    var fileId: String? = ""

    @JSONField(name = "file_name")
    var fileName: String? = ""

    @JSONField(name = "parent_file_id")
    var parentId: String? = ""

    @JSONField(name = "part_info_list")
    var partInfoList: ArrayList<PartInfo>? = null

    @JSONField(name = "rapid_upload")
    var rapidUpload: Boolean = false

    var exist: Boolean = false

    var status: String? = ""

    var type: String? = ""

    @JSONField(name = "upload_id")
    var uploadId: String? = ""

}