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

package com.aliyun.pds.sdk.upload

import com.aliyun.pds.sdk.*
import com.aliyun.pds.sdk.exception.*
import com.aliyun.pds.sdk.http.HTTPUtils
import com.aliyun.pds.sdk.model.*
import com.aliyun.pds.sdk.thread.ThreadPoolUtils
import com.aliyun.pds.sdk.utils.FileUtils
import okhttp3.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.Callable
import java.util.concurrent.Future

class UploadOperation(private val task: SDUploadTask) : Operation {

    var blockList: MutableList<UploadBlockInfo> = ArrayList()

    var currentSize: Long = 0

    private var progressLastUpdate: Long = 0
    private var taskFuture: Future<Any>? = null
    private var stopped = false

    private val uploadApi: UploadApi = UploadApi()
    private lateinit var uploadInfo: UploadInfo
    private val dao: UploadInfoDao = SDClient.instance.database.transferDB.uploadInfoDao()

    override fun execute() {

        stopped = false
        var exception: Exception? = null
        taskFuture = ThreadPoolUtils.instance.uploadTaskPool.submit(Callable<Any> {
            try {
                preAction()
                uploadAction()
            } catch (e: Exception) {
                exception = e
            }
            finish(exception)
        })
    }

    override fun stop() {
        stopped = true
        taskFuture?.cancel(true)
    }

    override fun cancel() {
        stop()
        dao.delete(uploadInfo)
    }

    fun preAction() {
        initBlock()
    }

    fun initBlock() {
        blockList.clear()
        val info = dao.getUploadInfo(task.taskId)
        if (null != info) {
            uploadInfo = info
            task.currentBlock = uploadInfo.currentBlock
            task.fileId = uploadInfo.fileId
            task.uploadId = uploadInfo.uploadId
            task.uploadState = SDUploadTask.UploadState.values()[uploadInfo.uploadState]
        } else {
            uploadInfo = UploadInfo()
            uploadInfo.taskId = task.taskId
            dao.insert(uploadInfo);
        }

        var blockCount: Int = (task.fileSize / SDConfig.miniBlock).toInt()
        if (0 == blockCount && task.fileSize > 0) {
            blockCount = 1
        } else if (blockCount >= SDConfig.maxBlockCount) {
            blockCount = SDConfig.maxBlockCount
        }
        val blockSize = task.fileSize / blockCount
        var remainder = 0L
        if (blockSize > 0) {
            remainder = task.fileSize % blockSize
        }

        if (task.currentBlock > 0) {
            currentSize = task.currentBlock * blockSize
        }

        for (i in 0 until blockCount) {
            val blockInfo = UploadBlockInfo(i * blockSize, blockSize, i + 1L, "")
            blockList.add(blockInfo)
        }
        if (blockList.isNotEmpty()) {
            blockList.last().size += remainder
        }
    }

    fun uploadAction() {

        while (SDBaseTask.TaskState.RUNNING == task.state && !stopped) {
            when {
                SDUploadTask.UploadState.FILE_CREATE == task.uploadState -> {
                    createFile(true)
                }
                SDUploadTask.UploadState.UPLOADING == task.uploadState -> {
                    uploading()
                }
                SDUploadTask.UploadState.COMPLETE == task.uploadState -> {
                    uploadComplete()
                    task.state = SDBaseTask.TaskState.FINISH
                }
            }
        }
    }

    fun createFile(needPreHash: Boolean) {
//        val params: MutableMap<String, Any?> = HashMap()
        val params = FileCreateRequest()
        if (!task.shareId.isNullOrEmpty()) {
            params.shareId = task.shareId
        } else if (!task.driveId.isNullOrEmpty()) {
            params.driveId = task.driveId
        }

        params.name = task.fileName
        params.parentFileId = task.parentId
        params.type = "file"
        params.contentType = task.mimeType
        if (needPreHash) {
            params.preHash = FileUtils.instance.fileRule1kSA1(task.filePath)
        } else {
            task.sha1 = FileUtils.instance.fileSHA1(task.filePath)
            params.contentHash = task.sha1
            params.contentHashName = "sha1"
        }
        if (SDClient.instance.config.canFastUpload) {
            params.size = task.fileSize
        }
        params.checkNameMode = "auto_rename"
        val list = ArrayList<PartInfo>()
        for (item in blockList) {
            val partInfo = PartInfo()
            partInfo.partNumber = item.num
            partInfo.partSize = item.size
            list.add(partInfo)
        }
        params.partInfoList = list
        var response: FileCreateResp?
        try {
            response = uploadApi.createFile(params)
        } catch (e: Exception) {
            if (e is InterruptedIOException && stopped) {
                return
            }
            throw SDNetworkException("create file error")
        }

        if (null == response) {
            throw SDNetworkException("create file error, response is null or json parse error")
        } else {
            if (201 == response.code) {
                task.fileId = response.fileId
                task.uploadId = response.uploadId
                if (response.rapidUpload || "available" == response.status) {
                    // 秒传
                    task.uploadState = SDUploadTask.UploadState.COMPLETE
                    currentSize = task.fileSize
                    task.progressListener?.onProgressChange(currentSize)
                    saveUploadInfo()
                } else {
                    task.uploadState = SDUploadTask.UploadState.UPLOADING
                    saveUploadInfo()
                    if (response.partInfoList != null) {
                        updateBlockUrl(response.partInfoList)
                    }
                }
            } else {
                if ("QuotaExhausted.Drive" == response.errorCode) {
                    // 空间不足
                    throw SpaceNotEnoughException("no space to create file")
                } else if ("InvalidParameter.SizeExceed" == response.errorCode) {
                    throw SDSizeExceedException("file size too big")
                } else if ("ForbiddenUpload" == response.errorCode) {
                    throw SDForbiddenException("code: ${response.errorCode} msg: ${response.errorMessage}")
                } else if ("PreHashMatched" == response.errorCode) {
                    if (needPreHash) {
                        createFile(false)
                    } else {
                        throw SDUnknownException("recursive pre hash match")
                    }
                } else if ("NotFound.File" == response.errorCode) {
                    //上传的目标文件夹不存在
                    throw FileNotFoundException(response.errorMessage)
                } else {
                    throw SDServerException(response.code,
                        "errorCode: ${response.errorCode} msg: ${response.errorMessage}")
                }
            }
        }
    }

    fun uploading() {
        if (blockList.isEmpty() || blockList[0].url.isNullOrEmpty()) {
            val fileInfo = getUploadUrl()
            if (fileInfo?.partInfoList != null) {
                for (i in fileInfo.partInfoList!!.indices) {
                    blockList[i].url = fileInfo.partInfoList!![i].uploadUrl
                }
            } else {
                throw SDServerException(200, "get empty upload url")
            }
        }

        val listener = object : HTTPUtils.OnTransferChangeListener {
            override fun onChange(size: Long) {
                currentSize += size
                val currentTime = System.currentTimeMillis()
                progressChange(currentTime)
            }

        }
        while (task.currentBlock < blockList.size) {
            val block = blockList[task.currentBlock]
            val url = block.url
            var resp: Response? = null
            try {
                resp = HTTPUtils.instance.uploadData(url!!,
                    File(task.filePath),
                    task.mimeType,
                    block.start,
                    block.size,
                    listener)
            } catch (e: Exception) {
                if (e is InterruptedIOException && stopped) {
                    return
                }
                e.printStackTrace()
                if (e is FileNotFoundException) {
                    throw e
                }
                if (e is IOException) {
                    throw SDNetworkException(e.message)
                } else {
                    throw SDUnknownException(e.message)
                }
            }
            if (null == resp) {
                throw SDNetworkException("upload error")
            }

            if (200 == resp.code || 409 == resp.code) {
                task.currentBlock += 1
                saveUploadInfo()
            } else if (403 == resp.code) {
                val fileInfo = getUploadUrl()
                if (null == fileInfo?.partInfoList) {
                    val e = SDServerException(200, "part info list is null")
                    e.printStackTrace()
                    throw e
                }
                updateBlockUrl(fileInfo?.partInfoList)
            }
        }
        if (task.currentBlock == blockList.size) {
            task.uploadState = SDUploadTask.UploadState.COMPLETE
            saveUploadInfo()
        }
    }

    fun updateBlockUrl(partInfoList: ArrayList<PartInfo>?) {
        for (i in partInfoList!!.indices) {
            if (i >= blockList.size) return
            blockList[i].url = partInfoList!![i].uploadUrl
        }
    }

    fun progressChange(currentTime: Long) {
        if (currentTime - progressLastUpdate > 300 || currentSize == task.fileSize) {
            progressLastUpdate = currentTime
            task.progressListener?.onProgressChange(currentSize)
        }
    }

    private fun saveUploadInfo() {
        uploadInfo.fileId = task.fileId!!
        uploadInfo.uploadId = task.uploadId!!
        uploadInfo.uploadState = task.uploadState.ordinal
        uploadInfo.currentBlock = task.currentBlock
        uploadInfo.taskId = task.taskId
        dao.update(uploadInfo)
    }

    fun getUploadUrl(): FileGetUploadUrlResp? {
        val params = FileGetUploadUrlRequest()
        if (!task.shareId.isNullOrEmpty()) {
            params.shareId = task.shareId
        } else if (!task.driveId.isNullOrEmpty()) {
            params.driveId = task.driveId
        }

        params.fileId = task.fileId
        params.uploadId = task.uploadId

        val list = ArrayList<PartInfo>()
        for (item in blockList) {
            val partInfo = PartInfo()
            partInfo.partNumber = item.num
            partInfo.partSize = item.size
            list.add(partInfo)
        }
        params.partInfoList = list
        var resp: FileGetUploadUrlResp? = null
        try {
            resp = uploadApi.getUploadUrl(params)
        } catch (e: Exception) {
            if (e is InterruptedIOException && stopped) {
                return resp
            }
            e.printStackTrace()
            if (e is IOException) {
                throw SDNetworkException(e.message)
            } else {
                throw SDUnknownException(e.message)
            }
        }
        if (null == resp) {
            throw SDNetworkException("get upload url error")
        }

        if (resp.code == 200) {
            return resp
        } else {
            throw SDServerException(resp.code,
                "errorCode: ${resp.errorCode} errorMsg: ${resp.errorMessage}")
        }
    }

    fun uploadComplete() {
        val params = FileCompleteRequest()
        if (!task.shareId.isNullOrEmpty()) {
            params.shareId = task.shareId
        } else if (!task.driveId.isNullOrEmpty()) {
            params.driveId = task.driveId
        }
        params.fileId = task.fileId
        params.uploadId = task.uploadId

        var resp: FileInfoResp?
        try {
            resp = uploadApi.fileComplete(params)
        } catch (e: Exception) {
            if (e is InterruptedIOException && stopped) {
                return
            }
            e.printStackTrace()
            if (e is IOException) {
                throw SDNetworkException(e.message)
            } else {
                throw SDUnknownException(e.message)
            }
        }
        if (null == resp) {
            throw SDNetworkException("file complete error")
        }
        if (resp.code == 200) {
            return
        } else {
            throw SDServerException(resp.code,
                "errorCode: ${resp.errorCode} errorMsg: ${resp.errorMessage}")
        }
    }


    private fun finish(e: Exception? = null) {
        if (stopped) {
            return
        }
        var errorInfo = covertFromException(e)
        task.completeListener?.onComplete(task.taskId,
            SDFileMeta(task.fileId, task.fileName, task.uploadId), errorInfo
        )
        cancel()
    }
}
