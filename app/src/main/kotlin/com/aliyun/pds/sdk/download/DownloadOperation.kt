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

import android.content.Context
import com.aliyun.pds.sdk.*
import com.aliyun.pds.sdk.exception.*
import com.aliyun.pds.sdk.http.HTTPUtils
import com.aliyun.pds.sdk.thread.ThreadPoolUtils
import com.aliyun.pds.sdk.thread.ThreadPoolWrap
import com.aliyun.pds.sdk.utils.FileUtils
import com.aliyun.pds.sdk.utils.LogUtils
import okhttp3.internal.toImmutableMap
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.Exception
import java.util.concurrent.*


internal open class DownloadOperation(
    private val context: Context,
    protected val task: SDDownloadTask,
    private val blockInfoDao: DownloadBlockInfoDao,
    protected val config: SDConfig,
    private val resultCheck: ResultCheck = CRC64Check(),
) : Operation {

    // 分片最小尺寸
    private val miniBlockSize = config.downloadBlockSize

    // 最大分片个数
    private val maxBlockCount = config.downloadMaxBlockCount

    private lateinit var tmpFile: File
    lateinit var blockList: MutableList<DownloadBlockInfo>
    val needSaveBlockList = ConcurrentHashMap<String, DownloadBlockInfo>()

    private var taskFuture: Future<*>? = null
    private var threadPool: ThreadPoolExecutor? = null

    private var downloadUrl = task.downloadUrl
    private var currentSize: Long = 0

    // user call stop
    private var stopped = false

    @Volatile
    private var progressLastUpdate: Long = 0

    @Volatile
    private var blockInfoLastUpdate: Long = 0

    override fun execute() {
        stopped = false
        var exception : Exception? = null
        taskFuture = ThreadPoolUtils.instance.downloadTaskPool.submit {
            try {
                preAction()
                download()
                resultAction()
            } catch (e: Exception) {
                exception = e
            }
            finish(exception)
        }
    }

    fun preAction() {
        spaceCheck(task.fileSize)
        downloadUrlCheck()
        initFile()
        initBlock()
        checkDownloadState()

        if (LogUtils.instance.isDebug) {
           LogUtils.instance.log("download pre action")
           LogUtils.instance.log("download url is ${task.downloadUrl}")
           LogUtils.instance.log("block count is ${blockList.size}")
        }
    }

    @Throws(Exception::class)
    fun download() {
        if (stopped) {
            return
        }
        threadPool?.shutdownNow()
        threadPool = ThreadPoolWrap(4, 4, "pds-sdk-download").pool
        val queue = ArrayBlockingQueue<Pair<DownloadBlockInfo, HTTPUtils.OnTransferChangeListener>>(
            maxBlockCount)
        val countDownLatch = CountDownLatch(blockList.size)
        // ide has a warning, kotlin Long is not Object???
        val currentSizeLock = "currentSizeLock"
        for (i in blockList.indices) {
            val start = blockList[i].start
            val offset = blockList[i].offset
            val end = blockList[i].end
            if (start + offset == end) {
                countDownLatch.countDown()
                continue
            }

            val blockInfo = blockList[i]
            val listener = object : HTTPUtils.OnTransferChangeListener {
                override fun onChange(size: Long) {
                    blockInfo.offset += size
                    synchronized(currentSizeLock) {
                        currentSize += size
                    }
                    needSaveBlockList[blockInfo.id.toString()] = blockInfo
                    progressChange()
                    saveBlockInfo()
                }
            }
            queue.add(Pair(blockInfo, listener))
        }
        var i = 0
        val limit = 4.coerceAtMost(blockList.size)
        var exception: Exception? = null
        val lock = ""
        while (i < limit) {
            threadPool?.submit(Callable<Any> {
                var pair = queue.poll()
                do {
                    val blockInfo = pair.first
                    val listener = pair.second
                    try {
                        downloadBlock(blockInfo.start, blockInfo.offset, blockInfo.end, listener)
                    } catch (e: Exception) {
                        synchronized(lock) {
                            if (null != exception) return@Callable
                            exception = e
                            for (i in 0 until countDownLatch.count) {
                                countDownLatch.countDown()
                            }
                        }
                        return@Callable
                    }
                    countDownLatch.countDown()
                    pair = queue.poll()
                } while (pair != null)
            })
            i++
        }
        try {
            countDownLatch.await()
            if (exception != null) {
                throw exception!!
            }
        } catch (e: InterruptedException) {
        }
    }


    fun resultAction() {
        if (stopped) {
            return
        }
        checkSum()
        renameAction()
        removeBlockInfo()
    }


    override fun stop(clean: Boolean) {
        stopped = true
        if (clean) {
            blockList.clear()
            blockInfoDao.delete(task.taskId)
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
        }
        threadPool?.shutdownNow()
        taskFuture?.cancel(true)
    }


    fun progressChange() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - progressLastUpdate > 300 || currentSize == task.fileSize) {
            progressLastUpdate = currentTime
            task.progressListener?.onProgressChange(currentSize)
        }
    }

    fun saveBlockInfo() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - blockInfoLastUpdate > 1000) {
            val saveMap = needSaveBlockList.toImmutableMap()
            needSaveBlockList.clear()
            blockInfoLastUpdate = currentTime
            for (info in saveMap.values) {
                blockInfoDao.update(info)
            }
        }
    }

    private fun finish(e: Exception? = null) {
        if (stopped) {
            return
        }
        val errorInfo = covertFromException(e)
        task.state = SDBaseTask.TaskState.FINISH
        task.completeListener?.onComplete(task.taskId,
            SDFileMeta(task.fileId, task.fileName, task.filePath, ""), errorInfo
        )
        stop(false)
    }


    private fun initFile() {
        val tmpDir = File(task.filePath).parentFile
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        val filePath = "${tmpDir.path}${File.separator}"
        if (filePath.contains(Regex("[\"*:<>?|\\\\]"))) {
            throw SDPathRuleErrorException("path error")
        }

        tmpFile = File("$filePath.${task.taskId}_${task.fileId}.tmp")
        if (!tmpFile.exists()) {
            // if name is too long resize to 64
            task.fileName = FileUtils.instance.renameByLength(64, task.fileName)
            // tmp file not found rm block info if exist
            blockInfoDao.delete(task.taskId)
            try {
                tmpFile.createNewFile()
                val randomAccess = RandomAccessFile(tmpFile, "rw")
                randomAccess.setLength(task.fileSize)
                randomAccess.close()
            } catch (e: IOException) {
                e.printStackTrace()
                throw SpaceNotEnoughException("file create error")
            }
        }
    }

    private fun initBlock() {
        blockList = blockInfoDao.getAll(task.taskId).toMutableList()
        if (blockList.isEmpty()) {
            var blockCount: Int = (task.fileSize / miniBlockSize).toInt()
            if (blockCount <= 0) {
                blockCount = 1
            }
            blockCount = if (blockCount < maxBlockCount) blockCount else maxBlockCount

            val blockSize = task.fileSize / blockCount
            var remainder = 0L
            if (blockSize > 0) {
                remainder = task.fileSize % blockSize
            }

            for (i in 0 until blockCount) {
                val blockInfo = DownloadBlockInfo()
                blockInfo.start = i * blockSize
                blockInfo.end = blockInfo.start + blockSize
                blockInfo.offset = 0
                blockInfo.taskId = task.taskId
                if (i == blockCount - 1) {
                    blockInfo.end += remainder
                }
                blockList.add(blockInfo)
            }
            if (blockList.isNotEmpty()) {
                val ids = blockInfoDao.insert(blockList)
                for (i in ids.indices) {
                    blockList[i].id = ids[i].toInt()
                }
            }
        }
    }

    private fun checkDownloadState() {

        val fileLength = getFileLength()
        if (fileLength >= 0 && fileLength < task.fileSize) {
            currentSize = fileLength
        } else if (fileLength > task.fileSize) {
            resetBlockInfo()
        }
    }

    private fun downloadBlock(
        start: Long,
        offset: Long,
        end: Long,
        listener: HTTPUtils.OnTransferChangeListener,
    ) {
        var retryCount = 0
        var success = false
        var exception: Exception? = null
        do {
            try {
                HTTPUtils.instance.downloadData(
                    downloadUrl!!,
                    tmpFile.path,
                    start,
                    offset,
                    end,
                    listener
                )
                success = true
            } catch (e: Exception) {
                exception = e
                if (e is DownloadUrl403Exception) {
                    val oldUrl = downloadUrl
                    synchronized(downloadUrl!!) {
                        if (oldUrl == downloadUrl) {
                            downloadUrl = refreshDownloadUrl()
                        }
                    }
                }
            }
            retryCount ++
        } while (retryCount < 2 && !success && !stopped)
        if (!success) {
            if (null != exception) {
                exception.printStackTrace()
                throw  exception
            }
        }
    }

    private fun getFileLength(): Long {
        var len: Long = 0
        for (info in blockList) {
            len += info.offset
        }
        return len
    }

    private fun resetBlockInfo() {
        for (info in blockList) {
            info.offset = 0
        }
    }

    private fun removeBlockInfo() {
        blockList.clear()
        blockInfoDao.delete(task.taskId)
    }

    private fun spaceCheck(fileSize: Long) {
        val freeSpace = context.getExternalFilesDir(null)?.freeSpace ?: 0
        if (fileSize + 1024 * 1024 * 10 > freeSpace) {
            throw SpaceNotEnoughException("space not enough")
        }
    }

    private fun downloadUrlCheck() {
        if (downloadUrl.isNullOrEmpty()) {
            downloadUrl = refreshDownloadUrl()
        }
    }

    private fun checkSum() {
        if (0L == task.fileSize || null == task.contentHash) {
            return
        }

        if (!tmpFile.exists()) {
            throw SDTmpFileNotExistException("tmp file not exist")
        }

        if (!resultCheck.check(tmpFile, task.contentHash)) {
            throw SDUnknownException("checkSum error")
        }
    }

    private fun renameAction() {
        val dir = File(task.filePath).parent
        task.fileName = FileUtils.instance.renameByRepeat(dir, task.fileName)
        val destFile = File(dir, task.fileName)
        task.filePath = destFile.path
        tmpFile.renameTo(destFile)
    }


    internal fun refreshDownloadUrl(): String {
        val resp = DownloadApi.instance.refreshDownloadUrl(task, config.downloadUrlExpiredTime)
        return if (null != resp && resp.code == 200 && !(resp.url.isNullOrEmpty())) {
            resp.url.toString()
        } else {
            if (null == resp) {
                throw SDNetworkException("network error")
            } else {
                throw SDServerException(resp.code, resp.errorCode, resp.requestId, resp.errorMessage)
            }
        }
    }
}
