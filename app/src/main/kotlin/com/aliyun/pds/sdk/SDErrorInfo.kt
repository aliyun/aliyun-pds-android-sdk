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

import com.aliyun.pds.sdk.exception.*
import java.io.FileNotFoundException
import java.lang.Exception

class SDErrorInfo(
    val code: SDTransferError,
    val message: String,
) {

}

fun covertFromException(exception: Exception?): SDErrorInfo {
    if (null == exception) {
        return SDErrorInfo(SDTransferError.None, "success")
    }
    when (exception) {
        is SDForbiddenException -> {
            return SDErrorInfo(SDTransferError.PermissionDenied, "no permission")
        }
        is SDNetworkException -> {
            return SDErrorInfo(SDTransferError.Network, "network error")
        }
        is SDSizeExceedException -> {
            return SDErrorInfo(SDTransferError.SizeExceed, "file is bigger than limit")
        }
        is SpaceNotEnoughException -> {
            return SDErrorInfo(SDTransferError.SpaceNotEnough, "space not enough")
        }
        is FileNotFoundException -> return SDErrorInfo(SDTransferError.FileNotExist, "file not found")

        is RemoteFileNotFoundException -> return SDErrorInfo(SDTransferError.RemoteFileNotExist, "remote file not found")

        is SDServerException -> {
            return SDErrorInfo(SDTransferError.Server, "server code is ${exception.code}, msg: ${exception.message}")
        }
        is ShareLinkCancelledException -> {
            return SDErrorInfo(SDTransferError.ShareLinkCancelled, "share link is cancelled")
        }
        is SDUnknownException -> return SDErrorInfo(SDTransferError.Unknown, "${exception.message}")
        else -> return SDErrorInfo(SDTransferError.Unknown, "${exception.message}")
    }
}
