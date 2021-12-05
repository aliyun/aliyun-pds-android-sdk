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

//package com.aliyun.pds.sdk.upload
//
//import com.alibaba.fastjson.annotation.JSONField
//
//
///**
// * 创建文件 Response, 获取分片信息公用
// */
//class FileInfoData {
//    var code = 0
//    var errorCode: String? = null
//    var errorMessage: String? = null
//
//    @JSONField(name = "domain_id")
//    var domainId: String? = null
//
//    @JSONField(name = "drive_id")
//    var driveId: String? = null
//
//    @JSONField(name = "file_path")
//    var filePath: String? = null
//
//    @JSONField(name = "file_id")
//    var fileId: String? = null
//
//    @JSONField(name = "rapid_upload")
//    var rapidUpload = false
//
//    @JSONField(name = "upload_id")
//    var uploadId: String? = null
//    var exist = false
//    var status: String? = null
//
//    @JSONField(name = "part_info_list")
//    var partInfoList: List<PartInfo>? = null
//
//    class PartInfo {
//        @JSONField(name = "part_number")
//        var partNumber = 0
//
//        @JSONField(name = "upload_url")
//        var uploadUrl: String? = null
//    }
//}