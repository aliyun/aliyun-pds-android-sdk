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


class FileListRequest : FileListBaseRequest() {
    @JSONField(name = "parent_file_id")
    var parentId: String = ""
}

class FileSearchRequest: FileListBaseRequest() {

    var query: String = ""

}

open class FileListBaseRequest {

    @JSONField(name = "domain_id")
    var domainId: String? = ""

    var starred: Boolean? = false

    var all: Boolean? = false

    var category: String? = ""

    @JSONField(name = "drive_id")
    var driveId: String? = ""

    var fields: String? = ""

    @JSONField(name = "image_thumbnail_process")
    var imageThumbnailProcess: String? = ""

    @JSONField(name = "image_url_process")
    var imageUrlProcess: String? = ""

    var limit: Long? = null

    var marker: String? = ""

    @JSONField(name = "order_by")
    var orderBy: String? = ""

    @JSONField(name = "order_direction")
    var orderDirection: String? = ""

    var status: String? = ""

    var type: String? = ""

    @JSONField(name = "url_expire_sec")
    var urlExpireSec: Long? = null

    @JSONField(name = "video_thumbnail_process")
    var videoThumbnailProcess: String? = ""


}