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
import android.util.Log
import com.aliyun.pds.sdk.Operation
import com.aliyun.pds.sdk.SDClient
import com.aliyun.pds.sdk.SDConfig
import com.aliyun.pds.sdk.exception.*
import com.aliyun.pds.sdk.http.HTTPUtils
import com.aliyun.pds.sdk.thread.ThreadPoolUtils
import com.aliyun.pds.sdk.thread.ThreadPoolWrap
import com.aliyun.pds.sdk.utils.FileUtils
import okhttp3.internal.toImmutableMap
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.Exception
import java.util.concurrent.*


class DownloadOperation(
    private val context: Context,
    private val task: SDDownloadTask,
    private val blockInfoDao: DownloadBlockInfoDao,
    private val config: SDConfig,
    private val resultCheck: ResultCheck,
) : Operation {

    // 分片最小尺寸
    private val miniBlockSize: Int = 1024 * 1024 * 10

    // 最大分片个数
    private val maxBlockCount: Int = 100

    private lateinit var tmpFile: File
    lateinit var blockList: MutableList<DownloadBlockInfo>
    val needSaveBlockList = ConcurrentHashMap<String, DownloadBlockInfo>()

    private var taskFuture: Future<*>? = null

    //    private var blocksFuture: MutableList<Future<*>> = arrayListOf()
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
        taskFuture = ThreadPoolUtils.instance.downloadTaskPool.submit {
            try {
                preAction()
                download()
                resultAction()
                finish()
            } catch (e: Exception) {
                errorHandle(e)
            }
        }
    }

    fun preAction() {
        spaceCheck(task.fileSize)
        downloadUrlCheck()
        initFile()
        initBlock()
        checkDownloadState()
    }

    fun download() {
        if (stopped) {
            return
        }
        threadPool?.shutdownNow()
        threadPool = ThreadPoolWrap(4, 4, "pds-sdk-download").pool
//        blocksFuture.forEach { it.cancel(true) }
//        blocksFuture.clear()
        val queue = ArrayBlockingQueue<Pair<DownloadBlockInfo, HTTPUtils.OnTransferChangeListener>>(
            maxBlockCount)
        val countDownLatch = CountDownLatch(blockList.size)
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
                    synchronized(currentSize) {
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
        val limit = 4.coerceAtMost(blockList.size);
        while (i < limit) {
            threadPool?.submit(Callable<Any> {
//                Log.d("threadCount",
//                    "->" + ThreadPoolUtils.instance.downloadConcurrentPool.activeCount)
                var pair = queue.poll()
                do {
                    val blockInfo = pair.first
                    val listener = pair.second
                    downloadBlock(blockInfo.start, blockInfo.offset, blockInfo.end, listener)
                    countDownLatch.countDown()
                    pair = queue.poll()
                } while (pair != null)
            })
//            blocksFuture.add(f)
            i++
        }
        try {
            countDownLatch.await()
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

    private fun errorHandle(e: Exception) {
        finish(e)
        cancel()
    }

    override fun stop() {
        stopped = true
        threadPool?.shutdownNow()
//        blocksFuture.forEach {
//            it.cancel(true)
//        }
        taskFuture?.cancel(true)
    }

    override fun cancel() {
        stop()
        blockList.clear()
        blockInfoDao.delete(task.taskId)

        if (tmpFile.exists()) {
            tmpFile.delete()
        }
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
        task.completeListener?.onComplete(
            mapOf(
                "taskId" to task.taskId,
                "fileName" to task.fileName,
            ), e
        )
    }


    private fun initFile() {
        val file = File(task.savePath)
        if (!file.exists()) {
            file.mkdirs()
        }

//        val tmpDir =
//            File(SDClient.instance.appContext.filesDir, SDConfig.downloadDir)
        val tmpDir = File(task.savePath)
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }
        tmpFile = File("${tmpDir.path}${File.separator}.${task.taskId}.tmp")
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
            if (blockCount <= 0 && task.fileSize > 0) {
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
                    blockList[i].id = ids[i]
                }
            }
        }
    }

    fun checkDownloadState() {

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
        val retryCount = 0
        var success = false
        var exception: Exception? = null
        do {
            try {
                HTTPUtils.instance.downloadData(
                    downloadUrl,
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
                    refreshDownloadUrl()
                }
            }
        } while (retryCount < 3 && !success && !stopped)
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
        blockList?.clear()
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
        if (!resultCheck.check(tmpFile, task)) {
            throw SDUnknownException("checkSum error")
        }
    }

    private fun renameAction() {
        val dir = File(task.savePath).path
        task.fileName = FileUtils.instance.renameByRepeat(dir, task.fileName)
        tmpFile.renameTo(File(dir, task.fileName))
    }


    private fun refreshDownloadUrl(): String {
        val resp = DownloadApi.instance.refreshDownloadUrl(task, config.downloadUrlExpiredTime)
        return if (null != resp && resp.code == 200 && !(resp.url.isNullOrEmpty())) {
            resp.url.toString()
        } else {
            if (null == resp) {
                throw SDNetworkException("network error")
            } else {
                if ("NotFound.File" == resp.errorCode) {
                    throw FileNotFoundException("file not found")
                }
                throw SDServerException(resp.code, resp.errorCode.toString())
            }
        }
    }
}
