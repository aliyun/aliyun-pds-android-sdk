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
    val exception: Exception?,
) {

    override fun toString(): String {
        return "code is $code, message is $message, exception is ${exception.toString()}"
    }
}

fun covertFromException(exception: Exception?): SDErrorInfo {
    if (null == exception) {
        return SDErrorInfo(SDTransferError.None, "success", null)
    }
    when (exception) {
        is SDForbiddenException -> {
            return SDErrorInfo(SDTransferError.PermissionDenied, "no permission", exception)
        }
        is SDNetworkException -> {
            return SDErrorInfo(SDTransferError.Network, "network error", exception)
        }
        is SDSizeExceedException -> {
            return SDErrorInfo(SDTransferError.SizeExceed, "file is bigger than limit", exception)
        }
        is SpaceNotEnoughException -> {
            return SDErrorInfo(SDTransferError.SpaceNotEnough, "space not enough", exception)
        }
        is FileNotFoundException -> return SDErrorInfo(SDTransferError.FileNotExist, "file not found", exception)

        is RemoteFileNotFoundException -> return SDErrorInfo(SDTransferError.RemoteFileNotExist, "remote file not found", exception)

        is SDServerException -> {
            return SDErrorInfo(SDTransferError.Server, "http code is ${exception.code}, error code is ${exception.erroCode},  msg: ${exception.message}", exception)
        }
        is ShareLinkCancelledException -> {
            return SDErrorInfo(SDTransferError.ShareLinkCancelled, "share link is cancelled", exception)
        }
        is SDUnknownException -> return SDErrorInfo(SDTransferError.Unknown, "${exception.message}", exception)
        is SDTmpFileNotExistException -> return SDErrorInfo(SDTransferError.TmpFileNotExist, "tmp file not exist", exception)
        is SDPathRuleErrorException -> return SDErrorInfo(SDTransferError.PathRuleError, "downlaod path rule error", exception)
        else -> return SDErrorInfo(SDTransferError.Unknown, "${exception.message}", exception)
    }
}
