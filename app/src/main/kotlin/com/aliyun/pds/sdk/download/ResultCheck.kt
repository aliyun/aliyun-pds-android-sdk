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

import com.aliyun.pds.sdk.utils.CRC64
import java.io.File

abstract class ResultCheck {

    abstract fun check(file: File, task: SDDownloadTask): Boolean
}


class CRC64Check : ResultCheck() {
    override fun check(file: File, task: SDDownloadTask): Boolean {
        if (task.crc64Hash.isNullOrEmpty()) return true
        return CRC64.fromFile(file)?.stringValue == task.crc64Hash
    }
}

class SizeCheck : ResultCheck() {

    override fun check(file: File, task: SDDownloadTask): Boolean {
        if (!file.exists()) return false
        return file.length() == task.fileSize
    }
}

class SHA1Check : ResultCheck() {

    override fun check(file: File, task: SDDownloadTask): Boolean {
        return true
    }

}