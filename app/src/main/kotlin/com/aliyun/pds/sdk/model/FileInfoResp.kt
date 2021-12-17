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

class FileInfoResp : BaseResp() {

    var category : String? = ""

    @JSONField(name = "content_hash")
    var contentHash: String? = ""

    @JSONField(name = "content_hash_name")
    var contentHashName: String? = ""

    @JSONField(name = "content_type")
    var contentType: String? = ""

    @JSONField(name = "crc64_hash")
    var crc64Hash: String? = ""

    @JSONField(name = "created_at")
    var create: String? = ""

    var description: String? = ""

    @JSONField(name = "domain_id")
    var domainId: String? = ""

    @JSONField(name = "download_url")
    var downloadUrl: String? = ""

    @JSONField(name = "drive_id")
    var driveId: String? = ""

    @JSONField(name = "file_extension")
    var fileExtension: String? = ""

    @JSONField(name = "file_id")
    var fileId: String? = ""

    @JSONField(name = "size")
    var fileSize: Long? = null

    var hidden: Boolean? = false

    @JSONField(name = "image_media_metadata")
    var imageMediaMetaData: ImageMediaResp? = null

    var labels: ArrayList<String>? = null

    var name: String? = ""

    @JSONField(name = "parent_file_id")
    var parentId: String? = ""

    var starred: Boolean? = false

    var status: String? = ""

    var thumbnail: String? = ""

    @JSONField(name = "trashed_at")
    var trashedAt: String? = ""

    var type: String? = ""

    @JSONField(name = "update_at")
    var update: String? = ""

    @JSONField(name = "upload_id")
    var uploadId: String? = ""

    var url: String? = ""

    @JSONField(name = "user_meta")
    var userMeta: String? = ""

    @JSONField(name = "video_media_metadata")
    var videoMediaMetaData: VideoMediaResp? = null
}

